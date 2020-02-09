package com.golovko.backend.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.dto.movieparticipation.MoviePartReadDTO;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
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

    @Test
    public void getMovieByIdTest() throws Exception {
        MovieReadDTO dto = createMovieReadDTO();

        Mockito.when(movieService.getMovie(dto.getId())).thenReturn(dto);

        String resultJSON = mockMvc
                .perform(get("/api/v1/movies/{id}", dto.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJSON, MovieReadDTO.class);
        Assertions.assertThat(actualMovie).isEqualToComparingFieldByField(dto);

        Mockito.verify(movieService).getMovie(dto.getId());
    }

    @Test
    public void getMovieWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);
        Mockito.when(movieService.getMovie(wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void createMovieTest() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("Guess Who");
        createDTO.setDescription("12345");
        createDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        createDTO.setIsReleased(false);

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.createMovie(createDTO)).thenReturn(readDTO);

        String result = mockMvc
                .perform(post("/api/v1/movies")
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
        patchDTO.setIsReleased(true);
        patchDTO.setReleaseDate(LocalDate.parse("1800-07-10"));

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.patchMovie(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJson, MovieReadDTO.class);
        Assert.assertEquals(readDTO, actualMovie);
    }

    @Test
    public void updateMovieTest() throws Exception {
        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("new title");
        updateDTO.setDescription("some NEW description");
        updateDTO.setIsReleased(false);
        updateDTO.setReleaseDate(LocalDate.parse("1900-07-10"));
        updateDTO.setAverageRating(5.5);

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.updateMovie(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(updateDTO))
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
    public void getMoviesWithFilter() throws Exception {
        MovieFilter filter = new MovieFilter();
        filter.setPersonId(UUID.randomUUID());
        filter.setPartTypes(Set.of(PartType.COMPOSER, PartType.WRITER));
        filter.setReleasedFrom(LocalDate.parse("1980-07-10"));
        filter.setReleasedTo(LocalDate.parse("1992-07-10"));

        MovieReadDTO movieReadDTO = new MovieReadDTO();
        movieReadDTO.setId(UUID.randomUUID());
        movieReadDTO.setMovieTitle("Title text");
        movieReadDTO.setDescription("Some text");
        movieReadDTO.setAverageRating(5.4);
        movieReadDTO.setReleaseDate(LocalDate.parse("1985-01-10"));
        movieReadDTO.setIsReleased(true);

        MoviePartReadDTO moviePartReadDTO = new MoviePartReadDTO();
        moviePartReadDTO.setMovieId(movieReadDTO.getId());
        moviePartReadDTO.setPersonId(filter.getPersonId());
        moviePartReadDTO.setPartType(PartType.WRITER);
        moviePartReadDTO.setId(UUID.randomUUID());
        moviePartReadDTO.setAverageRating(5.4);
        moviePartReadDTO.setPartInfo("Some text");

        List<MovieReadDTO> expectedResult = List.of(movieReadDTO);

        Mockito.when(movieService.getMovies(filter)).thenReturn(expectedResult);

        String resultJson = mockMvc.perform(get("/api/v1/movies")
            .param("personId", filter.getPersonId().toString())
            .param("partTypes", "COMPOSER, WRITER")
            .param("releasedFrom", filter.getReleasedFrom().toString())
            .param("releasedTo", filter.getReleasedTo().toString()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<MovieReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);
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
}