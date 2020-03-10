package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Rating;
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
@WebMvcTest(MovieRatingController.class)
public class MovieRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RatingService ratingService;

    @Test
    public void testGetMovieRatingById() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(5, authorId, movieId);

        Mockito.when(ratingService.getRating(movieId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/ratings/{ratingId}", movieId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(ratingService).getRating(movieId, readDTO.getId());
    }

    @Test
    public void testGetAllMovieRatings() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();
        RatingReadDTO r1 = createRatingReadDTO(7, authorId1, movieId);
        RatingReadDTO r2 = createRatingReadDTO(5, authorId2, movieId);

        List<RatingReadDTO> expectedResult = List.of(r1, r2);

        Mockito.when(ratingService.getAllRatingsByMovieId(movieId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/ratings/", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<RatingReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(RatingReadDTO::getId)
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());

        Mockito.verify(ratingService).getAllRatingsByMovieId(movieId);
    }

    @Test
    public void testGetMovieRatingByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Rating.class, wrongId, movieId);

        Mockito.when(ratingService.getRating(movieId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/ratings/{ratingId}", movieId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);

        RatingReadDTO readDTO = createRatingReadDTO(7, authorId, movieId);

        Mockito.when(ratingService.createRating(movieId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/ratings/", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultString, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(ratingService).createRating(movieId, createDTO);
    }

    @Test
    public void testUpdateMovieRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, userId, movieId);

        RatingPutDTO putDTO = new RatingPutDTO();
        putDTO.setRating(6);

        Mockito.when(ratingService.updateRating(movieId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/ratings/{ratingId}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchMovieRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, userId, movieId);

        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(9);

        Mockito.when(ratingService.patchRating(movieId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/ratings/{ratingId}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testDeleteMovieRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/ratings/{ratingId}", movieId, ratingId))
                .andExpect(status().isOk());

        Mockito.verify(ratingService).deleteRating(movieId, ratingId);
    }

    private RatingReadDTO createRatingReadDTO(int rating, UUID authorId, UUID targetObjectId) {
        RatingReadDTO dto = new RatingReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setRating(rating);
        dto.setAuthorId(authorId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        dto.setTargetObjectType(TargetObjectType.MOVIE);
        dto.setTargetObjectId(targetObjectId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
