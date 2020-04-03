package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.service.MovieGenreService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieGenreController.class)
public class MovieGenreControllerTest extends BaseControllerTest {

    @MockBean
    private MovieGenreService movieGenreService;

    @Test
    public void testGetGenresByMovieId() throws Exception {
        GenreReadDTO readDTO = createGenreReadDTO();
        List<GenreReadDTO> expectedResult = List.of(readDTO);

        UUID movieId = UUID.randomUUID();

        Mockito.when(movieGenreService.getMovieGenres(movieId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/genres", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<GenreReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(movieGenreService).getMovieGenres(movieId);
    }

    @Test
    public void testAddGenreToMovie() throws Exception {
        GenreReadDTO readDTO = createGenreReadDTO();
        UUID movieId = UUID.randomUUID();
        UUID genreId = readDTO.getId();

        List<GenreReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(movieGenreService.addGenreToMovie(movieId, genreId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/genres/{id}", movieId, genreId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<GenreReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(movieGenreService).addGenreToMovie(movieId, genreId);
    }

    @Test
    public void testRemoveGenreFromMovie() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID genreId = UUID.randomUUID();

        List<GenreReadDTO> emptyList = new ArrayList<>();

        Mockito.when(movieGenreService.removeGenreFromMovie(movieId, genreId)).thenReturn(emptyList);

        String resultJson = mockMvc
                .perform(delete("/api/v1/movies/{movieId}/genres/{id}", movieId, genreId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<GenreReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertTrue(actualResult.isEmpty());

        Mockito.verify(movieGenreService).removeGenreFromMovie(movieId, genreId);
    }

    private GenreReadDTO createGenreReadDTO() {
        return generateObject(GenreReadDTO.class);
    }
}
