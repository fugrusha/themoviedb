package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.BlockedUserException;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.CommentService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieCommentController.class)
public class MovieCommentControllerTest extends BaseControllerTest {

    @MockBean
    private CommentService commentService;

    @Test
    public void testGetMovieCommentById() throws Exception {
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieId);

        Mockito.when(commentService.getComment(movieId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/comments/{id}", movieId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).getComment(movieId, readDTO.getId());
    }

    @Test
    public void testGetAllPublishedMovieComments() throws Exception {
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getAllPublishedComments(movieId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/comments/", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getAllPublishedComments(movieId);
    }

    @Test
    public void testGetMovieCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(commentService.getComment(movieId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/comments/{id}", movieId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieComment() throws Exception {
        UUID movieId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);

        CommentReadDTO readDTO = createCommentReadDTO(movieId);

        Mockito.when(commentService.createComment(movieId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/comments/", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultString, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).createComment(movieId, createDTO);
    }

    @Test
    public void testCreateMovieCommentBlockedUserException() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);

        BlockedUserException exception = new BlockedUserException(authorId);

        Mockito.when(commentService.createComment(movieId, createDTO)).thenThrow(exception);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/comments/", movieId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultString.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieCommentNotNullValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/comments/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @Test
    public void testCreateMovieCommentMinSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/comments/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @Test
    public void testCreateMovieCommentMaxSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("comment message".repeat(100));
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/comments/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @Test
    public void testUpdateMovieComment() throws Exception {
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        Mockito.when(commentService.updateComment(movieId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/comments/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testUpdateMovieCommentMinSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("");

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).updateComment(any(), any(), any());
    }

    @Test
    public void testUpdateMovieCommentMaxSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("comment message".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).updateComment(any(), any(), any());
    }

    @Test
    public void testPatchMovieComment() throws Exception {
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        Mockito.when(commentService.patchComment(movieId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/comments/{id}", movieId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchMovieCommentMinSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).patchComment(any(), any(), any());
    }

    @Test
    public void testPatchMovieCommentMaxSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("comment message".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).patchComment(any(), any(), any());
    }

    @Test
    public void testDeleteMovieCommentTest() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/comments/{id}", movieId, commentId))
                .andExpect(status().isOk());

        Mockito.verify(commentService).deleteComment(movieId, commentId);
    }

    private CommentReadDTO createCommentReadDTO(UUID targetObjectId) {
        CommentReadDTO dto = new CommentReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMessage("some text");
        dto.setAuthorId(UUID.randomUUID());
        dto.setTargetObjectType(TargetObjectType.MOVIE);
        dto.setTargetObjectId(targetObjectId);
        dto.setDislikesCount(46);
        dto.setLikesCount(120);
        dto.setStatus(CommentStatus.PENDING);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
