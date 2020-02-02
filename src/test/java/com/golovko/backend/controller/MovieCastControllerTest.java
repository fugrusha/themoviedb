package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadExtendedDTO;
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

import java.time.LocalDate;
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
    public void getMovieCastTest() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        MovieCastReadDTO readDTO = createMovieCastDTO(movieId, personId);

        Mockito.when(movieCastService.getMovieCast(readDTO.getId())).thenReturn(readDTO);

        String result = mockMvc.perform(get("/api/v1/movie-cast/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(result, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieCastService).getMovieCast(readDTO.getId());
    }

    @Test
    public void getMovieCastExtendedTest() throws Exception {
        PersonReadDTO personReadDTO = createPersonReadDTO();
        MovieReadDTO movieReadDTO = createMovieReadDTO();
        MovieCastReadExtendedDTO extendedDTO = createMovieCastReadExtendedDTO(personReadDTO, movieReadDTO);

        Mockito.when(movieCastService.getMovieCastExtended(extendedDTO.getId())).thenReturn(extendedDTO);

        String resultJson = mockMvc.perform(get("/api/v1/movie-cast/{id}/extended", extendedDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, MovieCastReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(extendedDTO);

        Mockito.verify(movieCastService).getMovieCastExtended(extendedDTO.getId());
    }

    @Test
    public void getMovieCastWrongIdTest() throws Exception {
        UUID id = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(MovieCast.class, id);
        Mockito.when(movieCastService.getMovieCast(id)).thenThrow(exception);

        String resultJson = mockMvc.perform(get("/api/v1/movie-cast/{id}", id))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void deleteMovieCastTest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movie-cast/{id}", id.toString()))
                .andExpect(status().isOk());

        Mockito.verify(movieCastService).deleteMovieCast(id);
    }

    @Test
    public void createMovieCastTest() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        MovieCastReadDTO readDTO = createMovieCastDTO(movieId, personId);

        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPartInfo("Some text");
        createDTO.setCharacter("Vally");

        Mockito.when(movieCastService.createMovieCast(createDTO, movieId, personId)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/movie-cast/")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .param("movieId", movieId.toString())
                .param("personId", personId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(resultJson, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);
    }

    private MovieCastReadDTO createMovieCastDTO(UUID movieId, UUID personId) {
        MovieCastReadDTO dto = new MovieCastReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some Text");
        dto.setAverageRating(5.3);
        dto.setPartType(PartType.CAST);
        dto.setMovieId(movieId);
        dto.setPersonId(personId);
        dto.setCharacter("Leon Killer");
        return dto;
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

    public MovieCastReadExtendedDTO createMovieCastReadExtendedDTO(PersonReadDTO personDTO, MovieReadDTO movieDTO) {
        MovieCastReadExtendedDTO dto = new MovieCastReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);
        dto.setPartType(PartType.CAST);
        return dto;
    }
}
