package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.MovieCrewService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieCrewController.class)
public class MovieCrewControllerTest extends BaseControllerTest {

    @MockBean
    private MovieCrewService movieCrewService;

    @Test
    public void testGetMovieCrew() throws Exception {
        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();

        Mockito.when(movieCrewService.getMovieCrew(readDTO.getMovieId(), readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{id}",
                        readDTO.getMovieId(), readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCrewReadDTO actualDTO = objectMapper.readValue(resultJson, MovieCrewReadDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieCrewService).getMovieCrew(readDTO.getMovieId(), readDTO.getId());
    }

    @Test
    public void testGetMovieCrewExtended() throws Exception {
        PersonReadDTO personReadDTO = createPersonReadDTO();
        MovieReadDTO movieReadDTO = createMovieReadDTO();
        MovieCrewReadExtendedDTO readDTO = createMovieCrewReadExtendedDTO(personReadDTO, movieReadDTO);

        Mockito.when(movieCrewService.getExtendedMovieCrew(movieReadDTO.getId(), readDTO.getId()))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{id}/extended",
                        movieReadDTO.getId(), readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCrewReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, MovieCrewReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieCrewService).getExtendedMovieCrew(movieReadDTO.getId(), readDTO.getId());
    }

    @Test
    public void testGetAllMovieCrews() throws Exception {
        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();
        UUID movieId = readDTO.getMovieId();

        PageResult<MovieCrewReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(movieCrewService.getAllMovieCrews(movieId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MovieCrewReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @Test
    public void testGetMovieCastsWithPagingAndSorting() throws Exception {
        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();
        UUID movieId = readDTO.getMovieId();

        int page = 1;
        int size = 30;

        PageResult<MovieCrewReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(movieCrewService.getAllMovieCrews(movieId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews", movieId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MovieCrewReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetMovieCrewWrongId() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(MovieCrew.class, id, movieId);

        Mockito.when(movieCrewService.getMovieCrew(movieId, id)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{id}", movieId, id))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

    @WithMockUser
    @Test
    public void testCreateMovieCrew() throws Exception {
        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();
        UUID movieId = readDTO.getMovieId();

        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(readDTO.getPersonId());
        createDTO.setDescription("some text");
        createDTO.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);

        Mockito.when(movieCrewService.createMovieCrew(createDTO, movieId)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCrewReadDTO actualReadDTO = objectMapper.readValue(resultJson, MovieCrewReadDTO.class);
        Assertions.assertThat(actualReadDTO).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testCreateMovieCrewNotNullValidationException() throws Exception {
        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).createMovieCrew(any(), any());
    }

    @WithMockUser
    @Test
    public void testCreateMovieCrewMinSizeValidationException() throws Exception {
        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("");

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).createMovieCrew(any(), any());
    }

    @WithMockUser
    @Test
    public void testCreateMovieCrewMaxSizeValidationException() throws Exception {
        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("Some long long text".repeat(100));

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).createMovieCrew(any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCrew() throws Exception {
        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();
        UUID movieId = readDTO.getMovieId();

        MovieCrewPutDTO updateDTO = new MovieCrewPutDTO();
        updateDTO.setMovieCrewType(MovieCrewType.SOUND);
        updateDTO.setDescription("New text");
        updateDTO.setPersonId(UUID.randomUUID());

        Mockito.when(movieCrewService.updateMovieCrew(movieId, readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCrewReadDTO actualMovieParticipation = objectMapper.readValue(resultJson, MovieCrewReadDTO.class);
        Assertions.assertThat(actualMovieParticipation).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCrewMinSizeValidationException() throws Exception {
        MovieCrewPutDTO updateDTO = new MovieCrewPutDTO();
        updateDTO.setPersonId(UUID.randomUUID());
        updateDTO.setDescription("");

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).updateMovieCrew(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCrewMaxSizeValidationException() throws Exception {
        MovieCrewPutDTO updateDTO = new MovieCrewPutDTO();
        updateDTO.setPersonId(UUID.randomUUID());
        updateDTO.setDescription("Some long long text".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).updateMovieCrew(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchMovieCrew() throws Exception {
        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();
        patchDTO.setMovieCrewType(MovieCrewType.SOUND);
        patchDTO.setDescription("New text");
        patchDTO.setPersonId(UUID.randomUUID());

        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();
        UUID movieId = readDTO.getMovieId();

        Mockito.when(movieCrewService.patchMovieCrew(movieId, readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCrewReadDTO actualMovieParticipation = objectMapper.readValue(resultJson, MovieCrewReadDTO.class);
        Assertions.assertThat(actualMovieParticipation).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testPatchMovieCrewMinSizeValidationException() throws Exception {
        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();
        patchDTO.setPersonId(UUID.randomUUID());
        patchDTO.setDescription("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).patchMovieCrew(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchMovieCrewMaxSizeValidationException() throws Exception {
        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();
        patchDTO.setPersonId(UUID.randomUUID());
        patchDTO.setDescription("Some long long text".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCrewService, Mockito.never()).patchMovieCrew(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testDeleteMovieCrew() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-crews/{id}", movieId, id))
                .andExpect(status().is2xxSuccessful());

        Mockito.verify(movieCrewService).deleteMovieCrew(movieId, id);
    }

    private PersonReadDTO createPersonReadDTO() {
        return generateObject(PersonReadDTO.class);
    }

    private MovieReadDTO createMovieReadDTO() {
        return generateObject(MovieReadDTO.class);
    }

    private MovieCrewReadDTO createMovieCrewReadDTO() {
        return generateObject(MovieCrewReadDTO.class);
    }

    private MovieCrewReadExtendedDTO createMovieCrewReadExtendedDTO(
            PersonReadDTO personDTO,
            MovieReadDTO movieDTO
    ) {
        MovieCrewReadExtendedDTO dto = generateObject(MovieCrewReadExtendedDTO.class);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);
        return dto;
    }
}
