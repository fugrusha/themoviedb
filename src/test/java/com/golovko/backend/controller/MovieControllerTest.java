package com.golovko.backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.MovieCreateDTO;
import com.golovko.backend.dto.MoviePatchDTO;
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

import java.util.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private MovieReadDTO createMovieReadDTO() {
        MovieReadDTO readDTO = new MovieReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setMovieTitle("Guess Who");
        readDTO.setDescription("12345");
        readDTO.setReleaseDate(new Date(2019,12,5));
        readDTO.setReleased(false);
        readDTO.setAverageRating(8.3);
        return readDTO;
    }

    @Test
    public void getMovieByIdTest() throws Exception {
        MovieReadDTO dto = createMovieReadDTO();

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

        MovieReadDTO readDTO = createMovieReadDTO();

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
    public void patchMovieTest() throws Exception {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("title");
        patchDTO.setDescription("some description");
        patchDTO.setReleased(true);
        patchDTO.setReleaseDate(new Date(1990,07,10));
        patchDTO.setAverageRating(0.1);

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.patchMovie(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(patch("/api/v1/movies/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJson, MovieReadDTO.class);
        Assert.assertEquals(readDTO, actualMovie);
    }

    @Test
    public void testDeleteMovie() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{id}", id.toString()))
                .andExpect(status().isOk());

        Mockito.verify(movieService).deleteMovie(id);
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


}