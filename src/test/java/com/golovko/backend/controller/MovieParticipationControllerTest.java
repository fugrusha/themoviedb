package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.movieParticipation.MoviePartCreateDTO;
import com.golovko.backend.dto.movieParticipation.MoviePartReadDTO;
import com.golovko.backend.dto.movieParticipation.MoviePartReadExtendedDTO;
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

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
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

        Mockito.when(movieParticipationService.getMovieParticipation(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(get("/api/v1/movie-participations/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadDTO actualDTO = objectMapper.readValue(resultJson, MoviePartReadDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieParticipationService).getMovieParticipation(readDTO.getId());
    }

    @Test
    public void getMovieParticipationExtendedTest() throws Exception {
        PersonReadDTO personReadDTO = createPersonReadDTO();
        MovieReadDTO movieReadDTO = createMovieReadDTO();
        MoviePartReadExtendedDTO readDTO = createMoviePartReadExtendedDTO(personReadDTO, movieReadDTO);

        Mockito
                .when(movieParticipationService.getExtendedMovieParticipation(readDTO.getId()))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-participations/{id}/extended", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, MoviePartReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieParticipationService).getExtendedMovieParticipation(readDTO.getId());
    }

    @Test
    public void getMovieParticipationWrongIdTest() throws Exception {
        UUID id = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(MovieParticipation.class, id);
        Mockito.when(movieParticipationService.getMovieParticipation(id)).thenThrow(exception);

        String resultJson = mockMvc.perform(get("/api/v1/movie-participations/{id}", id))
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
        Set<PartType> types = new HashSet<>();
        types.add(PartType.WRITER);
        types.add(PartType.COSTUME_DESIGNER);
        createDTO.setPartTypes(types);

        Mockito
                .when(movieParticipationService.createMovieParticipation(createDTO, movieId, personId))
                .thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/movie-participations/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO))
                .param("personId", personId.toString())
                .param("movieId", movieId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MoviePartReadDTO actualReadDTO = objectMapper.readValue(resultJson, MoviePartReadDTO.class);
        Assertions.assertThat(actualReadDTO).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void deleteMovieParticipationTest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movie-participations/{id}", id.toString()))
                .andExpect(status().is2xxSuccessful());

        Mockito.verify(movieParticipationService).deleteMovieParticipation(id);
    }


    public PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setFirstName("Max");
        dto.setLastName("Popov");
        dto.setGender(Gender.MALE);
        return dto;
    }

    public MovieReadDTO createMovieReadDTO() {
        MovieReadDTO readDTO = new MovieReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setMovieTitle("Guess Who");
        readDTO.setDescription("12345");
        readDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        readDTO.setReleased(false);
        readDTO.setAverageRating(8.3);
        return readDTO;
    }

    public MoviePartReadDTO createMoviePartReadDTO() {
        MoviePartReadDTO dto = new MoviePartReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPersonId(UUID.randomUUID());
        dto.setMovieId(UUID.randomUUID());

        Set<PartType> types = new HashSet<>();
        types.add(PartType.WRITER);
        types.add(PartType.COSTUME_DESIGNER);
        dto.setPartTypes(types);
        return dto;
    }

    public MoviePartReadExtendedDTO createMoviePartReadExtendedDTO(PersonReadDTO personDTO, MovieReadDTO movieDTO) {
        MoviePartReadExtendedDTO dto = new MoviePartReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);

        Set<PartType> types = new HashSet<>();
        types.add(PartType.WRITER);
        types.add(PartType.COSTUME_DESIGNER);
        dto.setPartTypes(types);
        return dto;
    }
}
