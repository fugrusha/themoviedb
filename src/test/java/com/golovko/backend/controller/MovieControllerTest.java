package com.golovko.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.MovieCreateDTO;
import com.golovko.backend.dto.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.MovieService;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MovieController.class)
public class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    @Test
    public void getMovieByIdTest() throws Exception {
        MovieReadDTO dto = new MovieReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMovieTitle("Titanic");
        dto.setDescription("moving movie");
        dto.setReleaseDate(new Date(2019,12,5));
        dto.setReleased(true);

        Mockito.when(movieService.getMovie(dto.getId())).thenReturn(dto);

        String resultJSON = mockMvc.perform(get("/api/v1/movies/{id}", dto.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJSON,MovieReadDTO.class);
        Assertions.assertThat(actualMovie).isEqualToComparingFieldByField(dto);

        Mockito.verify(movieService).getMovie(dto.getId());
    }

    @Test
    public void createMovieTest() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("Guess Who");
        createDTO.setDescription("12345");
        createDTO.setReleaseDate(new Date(2019,12,5));
        createDTO.setReleased(false);

        MovieReadDTO readDTO = new MovieReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setMovieTitle("Guess Who");
        readDTO.setDescription("12345");
        readDTO.setReleaseDate(new Date(2019,12,5));
        createDTO.setReleased(false);

        Mockito.when(movieService.createMovie(createDTO)).thenReturn(readDTO);

        String result = mockMvc.perform(post("/api/v1/movies")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(result, MovieReadDTO.class);
        Assertions.assertThat(actualMovie).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void getMovieWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);
        Mockito.when(movieService.getMovie(wrongId)).thenThrow(exception);

        String result = mockMvc.perform(get("/api/v1/movies/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void getMovieIdTypeMismatchTest() throws Exception {
        String invalidId = "123";

        String result = mockMvc.perform(get("/api/v1/movies/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(MethodArgumentTypeMismatchException.class.getSimpleName()));
    }
}