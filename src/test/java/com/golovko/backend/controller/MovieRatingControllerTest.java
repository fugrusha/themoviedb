package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Rating;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.RatingService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieRatingController.class)
public class MovieRatingControllerTest extends BaseControllerTest {

    @MockBean
    private RatingService ratingService;

    @Test
    public void testGetMovieRatingById() throws Exception {
        UUID movieId = UUID.randomUUID();;
        RatingReadDTO readDTO = createRatingReadDTO(5, movieId);

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
        RatingReadDTO r1 = createRatingReadDTO(7, movieId);
        RatingReadDTO r2 = createRatingReadDTO(5, movieId);

        PageResult<RatingReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(r1, r2));

        Mockito.when(ratingService.getRatingsByTargetObjectId(movieId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/ratings/", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<RatingReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(RatingReadDTO::getId)
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());

        Mockito.verify(ratingService).getRatingsByTargetObjectId(movieId, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetMovieRatingsWithPagingAndSorting() throws Exception {
        UUID movieId = UUID.randomUUID();
        RatingReadDTO r1 = createRatingReadDTO(7, movieId);
        RatingReadDTO r2 = createRatingReadDTO(5, movieId);

        int page = 1;
        int size = 30;

        PageResult<RatingReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(r1, r2));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rating"));

        Mockito.when(ratingService.getRatingsByTargetObjectId(movieId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/ratings/", movieId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "rating,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<RatingReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
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

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE);

        RatingReadDTO readDTO = createRatingReadDTO(7, movieId);

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
    public void testCreateMovieRatingNotNullValidationException() throws Exception {
        RatingCreateDTO createDTO = new RatingCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/ratings/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).createRating(any(), any());
    }

    @Test
    public void testUpdateMovieRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, movieId);

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
        RatingReadDTO readDTO = createRatingReadDTO(4, movieId);

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

    @Test
    public void testCreateMovieRatingMinValueValidationException() throws Exception {
        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(0);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/ratings/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).createRating(any(), any());
    }

    @Test
    public void testCreateMovieRatingMaxValueValidationException() throws Exception {
        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(11);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/ratings/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).createRating(any(), any());
    }

    @Test
    public void testPatchMovieRatingMaxValueValidationException() throws Exception {
        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(11);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/ratings/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).patchRating(any(), any(), any());
    }

    @Test
    public void testPatchMovieRatingMinValueValidationException() throws Exception {
        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(0);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/ratings/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).patchRating(any(), any(), any());
    }

    @Test
    public void testUpdateMovieRatingMaxValueValidationException() throws Exception {
        RatingPutDTO putDTO = new RatingPutDTO();
        putDTO.setRating(11);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/ratings/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).updateRating(any(), any(), any());
    }

    @Test
    public void testUpdateMovieRatingMinValueValidationException() throws Exception {
        RatingPutDTO putDTO = new RatingPutDTO();
        putDTO.setRating(0);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/ratings/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).updateRating(any(), any(), any());
    }

    private RatingReadDTO createRatingReadDTO(int rating, UUID targetObjectId) {
        RatingReadDTO dto = new RatingReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setRating(rating);
        dto.setAuthorId(UUID.randomUUID());
        dto.setRatedObjectType(TargetObjectType.MOVIE);
        dto.setRatedObjectId(targetObjectId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
