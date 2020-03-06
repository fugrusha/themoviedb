package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.RatingService;
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
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MovieCrewRatingController.class)
public class MovieCrewRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RatingService ratingService;

    @Test
    public void testGetMovieCrewRatingById() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(5, authorId, movieCrewId);

        Mockito.when(ratingService.getRating(movieCrewId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/{ratingId}",
                        movieId, movieCrewId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(ratingService).getRating(movieCrewId, readDTO.getId());
    }

    @Test
    public void testGetAllMovieCrewRatings() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();
        RatingReadDTO r1 = createRatingReadDTO(7, authorId1, movieCrewId);
        RatingReadDTO r2 = createRatingReadDTO(5, authorId2, movieCrewId);

        List<RatingReadDTO> expectedResult = List.of(r1, r2);

        Mockito.when(ratingService.getAllRatingsByMovieId(movieCrewId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/",
                        movieId, movieCrewId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<RatingReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(RatingReadDTO::getId)
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());

        Mockito.verify(ratingService).getAllRatingsByMovieId(movieCrewId);
    }

    @Test
    public void testGetMovieCrewRatingByWrongId() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID wrongId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(ratingService.getRating(movieCrewId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/{ratingId}",
                        movieId, movieCrewId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieCrewRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CREW);

        RatingReadDTO readDTO = createRatingReadDTO(7, authorId, movieCrewId);

        Mockito.when(ratingService.createRating(movieCrewId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/",
                        movieId, movieCrewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultString, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(ratingService).createRating(movieCrewId, createDTO);
    }

    @Test
    public void testUpdateMovieCrewRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, userId, movieCrewId);

        RatingPutDTO putDTO = new RatingPutDTO();
        putDTO.setRating(6);

        Mockito.when(ratingService.updateRating(movieCrewId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/{ratingId}",
                        movieId, movieCrewId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchMovieCrewRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, userId, movieCrewId);

        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(9);

        Mockito.when(ratingService.patchRating(movieCrewId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/{ratingId}",
                        movieId, movieCrewId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testDeleteMovieCrewRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings/{ratingId}",
                movieId, movieCrewId, ratingId))
                .andExpect(status().isOk());

        Mockito.verify(ratingService).deleteRating(movieCrewId, ratingId);
    }

    private RatingReadDTO createRatingReadDTO(int rating, UUID authorId, UUID targetObjectId) {
        RatingReadDTO dto = new RatingReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setRating(rating);
        dto.setAuthorId(authorId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        dto.setTargetObjectType(TargetObjectType.MOVIE_CREW);
        dto.setTargetObjectId(targetObjectId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
