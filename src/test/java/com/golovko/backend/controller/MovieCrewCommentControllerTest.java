package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
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

@WebMvcTest(MovieCrewCommentController.class)
public class MovieCrewCommentControllerTest extends BaseControllerTest {

    @MockBean
    private CommentService commentService;

    @Test
    public void testGetMovieCrewCommentById() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCrewId);

        Mockito.when(commentService.getComment(movieCrewId, readDTO.getId()))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        UUID.randomUUID(), movieCrewId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).getComment(readDTO.getTargetObjectId(), readDTO.getId());
    }

    @Test
    public void testGetAllPublishedMovieCrewComments() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCrewId);

        PageResult<CommentReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(commentService.getPublishedComments(movieCrewId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
                        UUID.randomUUID(), movieCrewId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getPublishedComments(movieCrewId, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetMovieCrewCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Comment.class, wrongId, movieCrewId);

        Mockito.when(commentService.getComment(movieCrewId, wrongId)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        movieId, movieCrewId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @WithMockUser
    @Test
    public void testCreateMovieCrewComment() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CREW);

        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCrewId);

        Mockito.when(commentService.createComment(movieCrewId, createDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
                        UUID.randomUUID(), movieCrewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).createComment(readDTO.getTargetObjectId(), createDTO);
    }

    @WithMockUser
    @Test
    public void testCreateMovieCrewCommentNotNullValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
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
    public void testCreateMovieCrewCommentMinSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CREW);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
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
    public void testCreateMovieCrewCommentMaxSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("comment message".repeat(100));
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CREW);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
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
    public void testUpdateMovieCrewComment() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCrewId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        Mockito.when(commentService.updateComment(movieCrewId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        UUID.randomUUID(), movieCrewId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testUpdateMovieCrewCommentMinSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
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
    public void testUpdateMovieCrewCommentMaxSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();;
        putDTO.setMessage("comment message".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
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
    public void testPatchMovieCrewComment() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCrewId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        Mockito.when(commentService.patchComment(movieCrewId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        UUID.randomUUID(), movieCrewId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testPatchMovieCrewCommentMinSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
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
    public void testPatchMovieCrewCommentMaxSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("comment message".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
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
    public void testDeleteMovieCrewComment() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{commentId}",
                movieId, movieCrewId, commentId))
                .andExpect(status().isOk());

        Mockito.verify(commentService).deleteComment(movieCrewId, commentId);
    }

    @Test
    public void testGetPublishedMovieCrewCommentsWithPagingAndSorting() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(movieCrewId);

        int page = 1;
        int size = 30;

        PageResult<CommentReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(commentService.getPublishedComments(movieCrewId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/",
                        movieId, movieCrewId)
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
        dto.setTargetObjectType(TargetObjectType.MOVIE_CREW);
        dto.setTargetObjectId(targetObjectId);
        return dto;
    }
}
