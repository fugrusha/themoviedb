package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.MovieCastService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MovieCastController.class)
public class MovieCastControllerTest {

    @MockBean
    private MovieCastService movieCastService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

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

        List<MovieCastReadDTO> listOfMovieCast = List.of(readDTO);

        Mockito.when(movieCastService.getListOfMovieCast(movieId)).thenReturn(listOfMovieCast);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MovieCastReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(listOfMovieCast, actualResult);
    }

    @Test
    public void testGetMovieCastWrongId() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(MovieCast.class, id, Movie.class, movieId);
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
