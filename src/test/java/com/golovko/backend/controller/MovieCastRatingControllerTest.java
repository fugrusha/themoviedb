package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.ActionType;
import com.golovko.backend.domain.Rating;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.exception.ActionOfUserDuplicatedException;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieCastRatingController.class)
public class MovieCastRatingControllerTest extends BaseControllerTest {

    @MockBean
    private RatingService ratingService;

    @Test
    public void testGetMovieCrewRatingById() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(5, movieCastId);

        Mockito.when(ratingService.getRating(movieCastId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        movieId, movieCastId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultJson, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(ratingService).getRating(movieCastId, readDTO.getId());
    }

    @Test
    public void testGetAllMovieCrewRatings() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        RatingReadDTO r1 = createRatingReadDTO(7, movieCastId);
        RatingReadDTO r2 = createRatingReadDTO(5, movieCastId);

        PageResult<RatingReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(r1, r2));

        Mockito.when(ratingService.getRatingsByTargetObjectId(movieCastId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        movieId, movieCastId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<RatingReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(RatingReadDTO::getId)
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());

        Mockito.verify(ratingService).getRatingsByTargetObjectId(movieCastId, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetMovieCastRatingsWithPagingAndSorting() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        RatingReadDTO r1 = createRatingReadDTO(7, movieCastId);
        RatingReadDTO r2 = createRatingReadDTO(5, movieCastId);

        int page = 1;
        int size = 30;

        PageResult<RatingReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(r1, r2));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "rating"));

        Mockito.when(ratingService.getRatingsByTargetObjectId(movieCastId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        movieId, movieCastId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "rating,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<RatingReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetMovieCrewRatingByWrongId() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID wrongId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Rating.class, wrongId, movieCastId);

        Mockito.when(ratingService.getRating(movieCastId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        movieId, movieCastId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieCrewRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE_CAST);

        RatingReadDTO readDTO = createRatingReadDTO(7, movieCastId);

        Mockito.when(ratingService.createRating(movieCastId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        movieId, movieCastId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        RatingReadDTO actualResult = objectMapper.readValue(resultString, RatingReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(ratingService).createRating(movieCastId, createDTO);
    }

    @Test
    public void testCreateDuplicatedMovieCastRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE_CAST);

        RatingReadDTO readDTO = createRatingReadDTO(7, movieCastId);

        ActionOfUserDuplicatedException ex = new ActionOfUserDuplicatedException(
                createDTO.getAuthorId(), ActionType.ADD_RATING,
                createDTO.getRatedObjectType(), readDTO.getRatedObjectId());

        Mockito.when(ratingService.createRating(movieCastId, createDTO)).thenThrow(ex);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        movieId, movieCastId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo actualResult = objectMapper.readValue(resultString, ErrorInfo.class);
        Assert.assertEquals(actualResult.getMessage(), ex.getMessage());

        Mockito.verify(ratingService).createRating(movieCastId, createDTO);
    }

    @Test
    public void testCreateMovieCastRatingValidationException() throws Exception {
        RatingCreateDTO createDTO = new RatingCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).createRating(any(), any());
    }

    @Test
    public void testUpdateMovieCrewRating() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, movieCastId);

        RatingPutDTO putDTO = new RatingPutDTO();
        putDTO.setRating(6);

        Mockito.when(ratingService.updateRating(movieCastId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        movieId, movieCastId, readDTO.getId())
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
        UUID movieCastId = UUID.randomUUID();
        RatingReadDTO readDTO = createRatingReadDTO(4, movieCastId);

        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(9);

        Mockito.when(ratingService.patchRating(movieCastId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        movieId, movieCastId, readDTO.getId())
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
        UUID movieCastId = UUID.randomUUID();
        UUID ratingId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                movieId, movieCastId, ratingId))
                .andExpect(status().isOk());

        Mockito.verify(ratingService).deleteRating(movieCastId, ratingId);
    }

    @Test
    public void testCreateMovieCastRatingMinValueValidationException() throws Exception {
        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(0);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE_CAST);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).createRating(any(), any());
    }

    @Test
    public void testCreateMovieCastRatingMaxValueValidationException() throws Exception {
        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(11);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE_CAST);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).createRating(any(), any());
    }

    @Test
    public void testPatchMovieCastRatingMaxValueValidationException() throws Exception {
        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(11);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).patchRating(any(), any(), any());
    }

    @Test
    public void testPatchMovieCastRatingMinValueValidationException() throws Exception {
        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(0);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).patchRating(any(), any(), any());
    }

    @Test
    public void testUpdateMovieCastRatingMaxValueValidationException() throws Exception {
        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(11);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).updateRating(any(), any(), any());
    }

    @Test
    public void testUpdateMovieCastRatingMinValueValidationException() throws Exception {
        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(0);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings/{ratingId}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(ratingService, Mockito.never()).updateRating(any(), any(), any());
    }

    private RatingReadDTO createRatingReadDTO(int rating, UUID targetObjectId) {
        RatingReadDTO dto = generateObject(RatingReadDTO.class);
        dto.setRating(rating);
        dto.setRatedObjectType(TargetObjectType.MOVIE_CAST);
        dto.setRatedObjectId(targetObjectId);
        return dto;
    }
}
