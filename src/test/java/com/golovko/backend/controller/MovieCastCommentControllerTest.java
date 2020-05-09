package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieCastCommentController.class)
public class MovieCastCommentControllerTest extends BaseControllerTest {

    @MockBean
    private CommentService commentService;

    @Test
    public void testGetMovieCastCommentById() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCastId);

        Mockito.when(commentService.getComment(movieCastId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        movieId, movieCastId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).getComment(movieCastId, readDTO.getId());
    }

    @Test
    public void testGetAllPublishedMovieCastComments() throws Exception {
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCastId);

        PageResult<CommentReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(commentService.getPublishedComments(movieCastId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        movieId, movieCastId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getPublishedComments(movieCastId, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetMovieCastCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Comment.class, wrongId, movieCastId);

        Mockito.when(commentService.getComment(movieCastId, wrongId)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        movieId, movieCastId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @WithMockUser
    @Test
    public void testCreateMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setSpoiler("spoiler");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        CommentReadDTO readDTO = createCommentReadDTO(movieCastId);

        Mockito.when(commentService.createComment(movieCastId, createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        movieId, movieCastId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).createComment(movieCastId, createDTO);
    }

    @WithMockUser
    @Test
    public void testCreateMovieCastCommentBlockedUserException() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setSpoiler("spoiler");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        BlockedUserException exception = new BlockedUserException(createDTO.getAuthorId());

        Mockito.when(commentService.createComment(movieCastId, createDTO)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        movieId, movieCastId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @WithMockUser
    @Test
    public void testCreateMovieCastCommentNotNullValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @WithMockUser
    @Test
    public void testCreateMovieCastCommentMinSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("");
        createDTO.setSpoiler("");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @WithMockUser
    @Test
    public void testCreateMovieCastCommentMaxSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("comment message".repeat(100));
        createDTO.setSpoiler("spoiler".repeat(1000));
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCastId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");
        putDTO.setSpoiler("new spoiler");

        Mockito.when(commentService.updateComment(movieCastId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        movieId, movieCastId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCastCommentMinSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("");
        putDTO.setSpoiler("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).updateComment(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCastCommentMaxSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();;
        putDTO.setMessage("comment message".repeat(100));
        putDTO.setSpoiler("new spoiler".repeat(1000));

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).updateComment(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCastId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");
        patchDTO.setSpoiler("new spoiler");

        Mockito.when(commentService.patchComment(movieCastId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        movieId, movieCastId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testPatchMovieCastCommentMinSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("");
        patchDTO.setSpoiler("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).patchComment(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchMovieCastCommentMaxSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("comment message".repeat(100));
        patchDTO.setSpoiler("new spoiler".repeat(1000));

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).patchComment(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testDeleteMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{commentId}",
                movieId, movieCastId, commentId))
                .andExpect(status().isOk());

        Mockito.verify(commentService).deleteComment(movieCastId, commentId);
    }

    @Test
    public void testGetPublishedMovieCastCommentsWithPagingAndSorting() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCastId);

        int page = 1;
        int size = 30;

        PageResult<CommentReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(commentService.getPublishedComments(movieCastId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/",
                        movieId, movieCastId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    private CommentReadDTO createCommentReadDTO(UUID targetObjectId) {
        CommentReadDTO dto = generateObject(CommentReadDTO.class);
        dto.setTargetObjectType(TargetObjectType.MOVIE_CAST);
        dto.setTargetObjectId(targetObjectId);
        return dto;
    }
}
