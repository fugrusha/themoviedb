package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.GenreService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
public class GenreControllerTest extends BaseControllerTest {

    @MockBean
    private GenreService genreService;

    @Test
    public void testGetGenreById() throws Exception {
        GenreReadDTO readDTO = createGenreReadDTO();

        Mockito.when(genreService.getGenre(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/genres/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GenreReadDTO actualResult = objectMapper.readValue(resultJson, GenreReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(genreService).getGenre(readDTO.getId());
    }

    @Test
    public void testGetAllGenres() throws Exception {
        GenreReadDTO genre1 = createGenreReadDTO();
        GenreReadDTO genre2 = createGenreReadDTO();

        PageResult<GenreReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(genre1, genre2));

        Mockito.when(genreService.getGenres(PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/genres/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<GenreReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(GenreReadDTO::getId)
                .containsExactlyInAnyOrder(genre1.getId(), genre2.getId());

        Mockito.verify(genreService).getGenres(PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetGenresWithPagingAndSorting() throws Exception {
        GenreReadDTO genre1 = createGenreReadDTO();

        int page = 1;
        int size = 30;

        PageResult<GenreReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(genre1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(genreService.getGenres(pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/genres/")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<GenreReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetExtendedGenre() throws Exception {
        MovieReadDTO m1 = createMovieReadDTO();
        MovieReadDTO m2 = createMovieReadDTO();
        GenreReadExtendedDTO extendedDTO = createGenreReadExtendedDTO(List.of(m1, m2));

        Mockito.when(genreService.getExtendedGenre(extendedDTO.getId())).thenReturn(extendedDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/genres/{id}/extended", extendedDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GenreReadExtendedDTO actualResult = objectMapper.readValue(resultJson, GenreReadExtendedDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(extendedDTO);

        Mockito.verify(genreService).getExtendedGenre(extendedDTO.getId());
    }

    @Test
    public void testGetGenreByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(genreService.getGenre(wrongId)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(get("/api/v1/genres/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

    @Test
    public void testCreateGenre() throws Exception {
        GenreCreateDTO createDTO = new GenreCreateDTO();
        createDTO.setGenreName("genre name");
        createDTO.setDescription("some description");

        GenreReadDTO readDTO = createGenreReadDTO();

        Mockito.when(genreService.createGenre(createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/genres/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GenreReadDTO actualResult = objectMapper.readValue(resultJson, GenreReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testCreateGenreNotNullValidationException() throws Exception {
        GenreCreateDTO createDTO = new GenreCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/genres/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).createGenre(any());
    }

    @Test
    public void testCreateGenreMinSizeValidationException() throws Exception {
        GenreCreateDTO createDTO = new GenreCreateDTO();
        createDTO.setGenreName("");
        createDTO.setDescription("");

        String resultJson = mockMvc
                .perform(post("/api/v1/genres/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).createGenre(any());
    }

    @Test
    public void testCreateGenreMaxSizeValidationException() throws Exception {
        GenreCreateDTO createDTO = new GenreCreateDTO();
        createDTO.setGenreName("genre name".repeat(100));
        createDTO.setDescription("genre description".repeat(100));

        String resultJson = mockMvc
                .perform(post("/api/v1/genres/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).createGenre(any());
    }

    @Test
    public void testPatchGenre() throws Exception {
        GenrePatchDTO patchDTO = new GenrePatchDTO();
        patchDTO.setGenreName("new genre");
        patchDTO.setDescription("new text description");

        GenreReadDTO readDTO = createGenreReadDTO();

        Mockito.when(genreService.patchGenre(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/genres/{id}", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GenreReadDTO actualResult = objectMapper.readValue(resultJson, GenreReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchGenreMinSizeValidationException() throws Exception {
        GenrePatchDTO patchDTO = new GenrePatchDTO();
        patchDTO.setGenreName("");
        patchDTO.setDescription("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/genres/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).patchGenre(any(), any());
    }

    @Test
    public void testPatchGenreMaxSizeValidationException() throws Exception {
        GenrePatchDTO patchDTO = new GenrePatchDTO();
        patchDTO.setGenreName("genre name".repeat(100));
        patchDTO.setDescription("genre description".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/genres/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).patchGenre(any(), any());
    }

    @Test
    public void testUpdateGenre() throws Exception {
        GenrePutDTO putDTO = new GenrePutDTO();
        putDTO.setGenreName("new genre");
        putDTO.setDescription("new text description");

        GenreReadDTO readDTO = createGenreReadDTO();

        Mockito.when(genreService.updateGenre(readDTO.getId(), putDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/genres/{id}", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GenreReadDTO actualResult = objectMapper.readValue(resultJson, GenreReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }


    @Test
    public void testUpdateGenreMinSizeValidationException() throws Exception {
        GenrePutDTO putDTO = new GenrePutDTO();
        putDTO.setGenreName("");
        putDTO.setDescription("");

        String resultJson = mockMvc
                .perform(put("/api/v1/genres/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).updateGenre(any(), any());
    }

    @Test
    public void testUpdateGenreMaxSizeValidationException() throws Exception {
        GenrePutDTO putDTO = new GenrePutDTO();
        putDTO.setGenreName("genre name".repeat(100));
        putDTO.setDescription("genre description".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/genres/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(genreService, Mockito.never()).updateGenre(any(), any());
    }

    @Test
    public void testDeleteGenre() throws Exception {
        UUID genreId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/genres/{id}", genreId))
                .andExpect(status().is2xxSuccessful());

        Mockito.verify(genreService).deleteGenre(genreId);
    }

    private GenreReadDTO createGenreReadDTO() {
        return generateObject(GenreReadDTO.class);
    }

    private GenreReadExtendedDTO createGenreReadExtendedDTO(List<MovieReadDTO> movies) {
        GenreReadExtendedDTO dto = generateObject(GenreReadExtendedDTO.class);
        dto.setMovies(movies);
        return dto;
    }

    private MovieReadDTO createMovieReadDTO() {
        return generateObject(MovieReadDTO.class);
    }
}
