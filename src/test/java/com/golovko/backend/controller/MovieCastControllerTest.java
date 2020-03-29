package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.MovieCastService;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieCastController.class)
public class MovieCastControllerTest extends BaseControllerTest {

    @MockBean
    private MovieCastService movieCastService;

    @Test
    public void testGetMovieCast() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        MovieCastReadDTO readDTO = createMovieCastReadDTO(movieId, personId);

        Mockito.when(movieCastService.getMovieCast(readDTO.getId(), movieId)).thenReturn(readDTO);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{id}", movieId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(result, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieCastService).getMovieCast(readDTO.getId(), movieId);
    }

    @Test
    public void testGetMovieCastExtended() throws Exception {
        PersonReadDTO personReadDTO = createPersonReadDTO();
        MovieReadDTO movieReadDTO = createMovieReadDTO();
        MovieCastReadExtendedDTO extendedDTO = createMovieCastReadExtendedDTO(personReadDTO, movieReadDTO);

        Mockito.when(movieCastService.getMovieCastExtended(extendedDTO.getId(), movieReadDTO.getId()))
                .thenReturn(extendedDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{id}/extended",
                                            movieReadDTO.getId(), extendedDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, MovieCastReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(extendedDTO);

        Mockito.verify(movieCastService).getMovieCastExtended(extendedDTO.getId(), movieReadDTO.getId());
    }

    @Test
    public void testGetAllMovieCasts() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        MovieCastReadDTO readDTO = createMovieCastReadDTO(movieId, personId);

        PageResult<MovieCastReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(movieCastService.getAllMovieCasts(movieId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MovieCastReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @Test
    public void testGetMovieCastsWithPagingAndSorting() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        MovieCastReadDTO readDTO = createMovieCastReadDTO(movieId, personId);

        int page = 1;
        int size = 30;

        PageResult<MovieCastReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(movieCastService.getAllMovieCasts(movieId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts", movieId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MovieCastReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetMovieCastWrongId() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(MovieCast.class, id, movieId);
        Mockito.when(movieCastService.getMovieCast(id, movieId)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{id}", movieId, id))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void testDeleteMovieCast() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-casts/{id}", movieId, id))
                .andExpect(status().isOk());

        Mockito.verify(movieCastService).deleteMovieCast(id, movieId);
    }

    @Test
    public void testCreateMovieCast() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        MovieCastReadDTO readDTO = createMovieCastReadDTO(movieId, personId);

        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPersonId(personId);
        createDTO.setDescription("Some text");
        createDTO.setCharacter("Vally");

        Mockito.when(movieCastService.createMovieCast(createDTO, movieId)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(resultJson, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testCreateMovieCastNotNullValidationException() throws Exception {
        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).createMovieCast(any(), any());
    }

    @Test
    public void testCreateMovieCastMinSizeValidationException() throws Exception {
        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("");
        createDTO.setCharacter("");

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).createMovieCast(any(), any());
    }

    @Test
    public void testCreateMovieCastMaxSizeValidationException() throws Exception {
        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("Some long long text".repeat(100));
        createDTO.setCharacter("Vally".repeat(100));

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).createMovieCast(any(), any());
    }

    @Test
    public void testUpdateMovieCast() throws Exception {
        MovieCastPutDTO updateDTO = new MovieCastPutDTO();
        updateDTO.setCharacter("New Character");
        updateDTO.setDescription("New text");
        updateDTO.setPersonId(UUID.randomUUID());

        UUID movieId = UUID.randomUUID();

        MovieCastReadDTO readDTO = createMovieCastReadDTO(movieId, updateDTO.getPersonId());

        Mockito.when(movieCastService.updateMovieCast(updateDTO, readDTO.getId(), movieId))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(resultJson, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testUpdateMovieCastMinSizeValidationException() throws Exception {
        MovieCastPutDTO updateDTO = new MovieCastPutDTO();
        updateDTO.setPersonId(UUID.randomUUID());
        updateDTO.setDescription("");
        updateDTO.setCharacter("");

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).updateMovieCast(any(), any(), any());
    }

    @Test
    public void testUpdateMovieCastMaxSizeValidationException() throws Exception {
        MovieCastPutDTO updateDTO = new MovieCastPutDTO();
        updateDTO.setPersonId(UUID.randomUUID());
        updateDTO.setDescription("Some long long text".repeat(100));
        updateDTO.setCharacter("Vally".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).updateMovieCast(any(), any(), any());
    }

    @Test
    public void testPatchMovieCast() throws Exception {
        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();
        patchDTO.setCharacter("New Character");
        patchDTO.setDescription("New text");
        patchDTO.setPersonId(UUID.randomUUID());

        UUID movieId = UUID.randomUUID();

        MovieCastReadDTO readDTO = createMovieCastReadDTO(movieId, patchDTO.getPersonId());

        Mockito.when(movieCastService.patchMovieCast(patchDTO, readDTO.getId(), movieId))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(resultJson, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchMovieCastMinSizeValidationException() throws Exception {
        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();
        patchDTO.setPersonId(UUID.randomUUID());
        patchDTO.setDescription("");
        patchDTO.setCharacter("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).patchMovieCast(any(), any(), any());
    }

    @Test
    public void testPatchMovieCastMaxSizeValidationException() throws Exception {
        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();
        patchDTO.setPersonId(UUID.randomUUID());
        patchDTO.setDescription("Some long long text".repeat(100));
        patchDTO.setCharacter("Vally".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieCastService, Mockito.never()).patchMovieCast(any(), any(), any());
    }

    private MovieCastReadDTO createMovieCastReadDTO(UUID movieId, UUID personId) {
        MovieCastReadDTO dto = new MovieCastReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setDescription("Some Text");
        dto.setAverageRating(5.3);
        dto.setMovieCrewType(MovieCrewType.CAST);
        dto.setMovieId(movieId);
        dto.setPersonId(personId);
        dto.setCharacter("Leon Killer");
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }

    private PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setFirstName("Max");
        dto.setLastName("Popov");
        dto.setGender(Gender.MALE);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }

    private MovieReadDTO createMovieReadDTO() {
        MovieReadDTO readDTO = new MovieReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setMovieTitle("Guess Who");
        readDTO.setDescription("12345");
        readDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        readDTO.setIsReleased(false);
        readDTO.setAverageRating(8.3);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return readDTO;
    }

    private MovieCastReadExtendedDTO createMovieCastReadExtendedDTO(
            PersonReadDTO personDTO,
            MovieReadDTO movieDTO
    ) {
        MovieCastReadExtendedDTO dto = new MovieCastReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setDescription("Some text");
        dto.setAverageRating(9.2);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);
        dto.setMovieCrewType(MovieCrewType.CAST);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
