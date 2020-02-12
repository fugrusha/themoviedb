package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.movieparticipation.*;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.MovieParticipationService;
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
@WebMvcTest(MovieParticipationController.class)
public class MovieParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieParticipationService movieParticipationService;

    @Test
    public void getMovieParticipationTest() throws Exception {
        MoviePartReadDTO readDTO = createMoviePartReadDTO();

        Mockito.when(movieParticipationService.getMovieParticipation(readDTO.getMovieId(), readDTO.getId()))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/{movieId}/movie-participations/{id}",
                        readDTO.getMovieId(), readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadDTO actualDTO = objectMapper.readValue(resultJson, MoviePartReadDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieParticipationService).getMovieParticipation(readDTO.getMovieId(), readDTO.getId());
    }

    @Test
    public void getMovieParticipationExtendedTest() throws Exception {
        PersonReadDTO personReadDTO = createPersonReadDTO();
        MovieReadDTO movieReadDTO = createMovieReadDTO();
        MoviePartReadExtendedDTO readDTO = createMoviePartReadExtendedDTO(personReadDTO, movieReadDTO);

        Mockito
                .when(movieParticipationService.getExtendedMovieParticipation(movieReadDTO.getId(), readDTO.getId()))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/{movieId}/movie-participations/{id}/extended",
                        movieReadDTO.getId(), readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, MoviePartReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieParticipationService)
                .getExtendedMovieParticipation(movieReadDTO.getId(), readDTO.getId());
    }

    @Test
    public void getListOfMovieParticipationTest() throws Exception {
        MoviePartReadDTO readDTO = createMoviePartReadDTO();
        UUID movieId = readDTO.getMovieId();

        List<MoviePartReadDTO> listOfMoviePart = List.of(readDTO);

        Mockito.when(movieParticipationService.getListOfMovieParticipation(movieId))
                .thenReturn(listOfMoviePart);

        String resultJson = mockMvc
                .perform(get("/api/v1/{movieId}/movie-participations", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MoviePartReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(listOfMoviePart, actualResult);
    }

    @Test
    public void getMovieParticipationWrongIdTest() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(MovieParticipation.class, id);
        Mockito.when(movieParticipationService.getMovieParticipation(movieId, id)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(get("/api/v1/{movieId}/movie-participations/{id}", movieId, id))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void createMovieParticipationTest() throws Exception {
        MoviePartReadDTO readDTO = createMoviePartReadDTO();
        UUID personId = readDTO.getPersonId();
        UUID movieId = readDTO.getMovieId();

        MoviePartCreateDTO createDTO = new MoviePartCreateDTO();
        createDTO.setPartInfo("some text");
        createDTO.setPartType(PartType.COSTUME_DESIGNER);

        Mockito
                .when(movieParticipationService.createMovieParticipation(createDTO, movieId, personId))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/{movieId}/movie-participations", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO))
                .param("personId", personId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadDTO actualReadDTO = objectMapper.readValue(resultJson, MoviePartReadDTO.class);
        Assertions.assertThat(actualReadDTO).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void updateMovieParticipationTest() throws Exception {
        MoviePartReadDTO readDTO = createMoviePartReadDTO();
        UUID movieId = readDTO.getMovieId();

        MoviePartPutDTO updateDTO = new MoviePartPutDTO();
        updateDTO.setPartType(PartType.COMPOSER);
        updateDTO.setPartInfo("New text");
        updateDTO.setPersonId(UUID.randomUUID());

        Mockito.when(movieParticipationService.updateMovieParticipation(movieId, readDTO.getId(), updateDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/{movieId}/movie-participations/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadDTO actualMovieParticipation = objectMapper.readValue(resultJson, MoviePartReadDTO.class);
        Assertions.assertThat(actualMovieParticipation).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchMovieParticipationTest() throws Exception {
        MoviePartPatchDTO patchDTO = new MoviePartPatchDTO();
        patchDTO.setPartType(PartType.COMPOSER);
        patchDTO.setPartInfo("New text");
        patchDTO.setPersonId(UUID.randomUUID());

        MoviePartReadDTO readDTO = createMoviePartReadDTO();
        UUID movieId = readDTO.getMovieId();

        Mockito.when(movieParticipationService.patchMovieParticipation(movieId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/{movieId}/movie-participations/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadDTO actualMovieParticipation = objectMapper.readValue(resultJson, MoviePartReadDTO.class);
        Assertions.assertThat(actualMovieParticipation).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void deleteMovieParticipationTest() throws Exception {
        UUID id = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/{movieId}/movie-participations/{id}", movieId, id.toString()))
                .andExpect(status().is2xxSuccessful());

        Mockito.verify(movieParticipationService).deleteMovieParticipation(movieId, id);
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

    private MoviePartReadDTO createMoviePartReadDTO() {
        MoviePartReadDTO dto = new MoviePartReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPersonId(UUID.randomUUID());
        dto.setMovieId(UUID.randomUUID());
        dto.setPartType(PartType.COSTUME_DESIGNER);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }

    private MoviePartReadExtendedDTO createMoviePartReadExtendedDTO(
            PersonReadDTO personDTO,
            MovieReadDTO movieDTO
    ) {
        MoviePartReadExtendedDTO dto = new MoviePartReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);
        dto.setPartType(PartType.COSTUME_DESIGNER);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
