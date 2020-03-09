package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.MovieCrewService;
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
@WebMvcTest(MovieCrewController.class)
public class MovieCrewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

        List<MovieCrewReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(movieCrewService.getAllMovieCrews(movieId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MovieCrewReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testGetMovieCrewWrongId() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(MovieCrew.class, id, Movie.class, movieId);

        Mockito.when(movieCrewService.getMovieCrew(movieId, id)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{id}", movieId, id))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

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

    @Test
    public void testUpdateMovieCrew() throws Exception {
        MovieCrewReadDTO readDTO = createMovieCrewReadDTO();
        UUID movieId = readDTO.getMovieId();

        MovieCrewPutDTO updateDTO = new MovieCrewPutDTO();
        updateDTO.setMovieCrewType(MovieCrewType.COMPOSER);
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

    @Test
    public void testPatchMovieCrew() throws Exception {
        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();
        patchDTO.setMovieCrewType(MovieCrewType.COMPOSER);
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

    @Test
    public void testDeleteMovieCrew() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-crews/{id}", movieId, id))
                .andExpect(status().is2xxSuccessful());

        Mockito.verify(movieCrewService).deleteMovieCrew(movieId, id);
    }


    private PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setFirstName("Max");
        dto.setLastName("Popov");
        dto.setGender(Gender.MALE);
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
        return readDTO;
    }

    private MovieCrewReadDTO createMovieCrewReadDTO() {
        MovieCrewReadDTO dto = new MovieCrewReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setDescription("Some text");
        dto.setAverageRating(9.2);
        dto.setPersonId(UUID.randomUUID());
        dto.setMovieId(UUID.randomUUID());
        dto.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }

    private MovieCrewReadExtendedDTO createMovieCrewReadExtendedDTO(
            PersonReadDTO personDTO,
            MovieReadDTO movieDTO
    ) {
        MovieCrewReadExtendedDTO dto = new MovieCrewReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setDescription("Some text");
        dto.setAverageRating(9.2);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);
        dto.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
