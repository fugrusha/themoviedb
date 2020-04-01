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
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleCommentController.class)
public class ArticleCommentControllerTest extends BaseControllerTest {

    @MockBean
    private CommentService commentService;

    @Test
    public void testGetArticleCommentById() throws Exception {
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(articleId);

        Mockito.when(commentService.getComment(articleId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/{id}", articleId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).getComment(articleId, readDTO.getId());
    }

    @Test
    public void testGetAllPublishedArticleComments() throws Exception {
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(articleId);

        PageResult<CommentReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(commentService.getPublishedComments(articleId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getPublishedComments(articleId, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetArticleCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Comment.class, wrongId, articleId);

        Mockito.when(commentService.getComment(articleId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/{id}", articleId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.ARTICLE);

        CommentReadDTO readDTO = createCommentReadDTO(articleId);

        Mockito.when(commentService.createComment(articleId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/articles/{articleId}/comments/", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultString, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).createComment(articleId, createDTO);
    }


    @Test
    public void testCreateArticleCommentBlockedUserException() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.ARTICLE);

        BlockedUserException exception = new BlockedUserException(authorId);

        Mockito.when(commentService.createComment(articleId, createDTO)).thenThrow(exception);

        String resultString = mockMvc
                .perform(post("/api/v1/articles/{articleId}/comments/", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultString.contains(exception.getMessage()));
    }

    @Test
    public void testCreateArticleCommentNotNullValidationFailed() throws Exception {
        UUID articleId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/{articleId}/comments/", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @Test
    public void testCreateArticleCommentMinSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.ARTICLE);

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/{articleId}/comments/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @Test
    public void testCreateArticleCommentMaxSizeValidationFailed() throws Exception {
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("comment message".repeat(100));
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(TargetObjectType.ARTICLE);

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/{articleId}/comments/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).createComment(any(), any());
    }

    @Test
    public void testUpdateArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(articleId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        Mockito.when(commentService.updateComment(articleId, readDTO.getId(), putDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{articleId}/comments/{id}", articleId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testUpdateArticleCommentMinSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("");

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{articleId}/comments/{id}",
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
    public void testUpdateArticleCommentMaxSizeValidationFailed() throws Exception {
        CommentPutDTO putDTO = new CommentPutDTO();;
        putDTO.setMessage("comment message".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{articleId}/comments/{id}",
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
    public void testPatchArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(articleId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        Mockito.when(commentService.patchComment(articleId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{articleId}/comments/{id}", articleId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchArticleCommentMinSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{articleId}/comments/{id}",
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
    public void testPatchArticleCommentMaxSizeValidationFailed() throws Exception {
        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("comment message".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{articleId}/comments/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        objectMapper.readValue(resultJson, ErrorInfo.class);
        Mockito.verify(commentService, Mockito.never()).patchComment(any(), any(), any());
    }

    @Test
    public void testDeleteArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/articles/{articleId}/comments/{id}", articleId, commentId))
                .andExpect(status().isOk());

        Mockito.verify(commentService).deleteComment(articleId, commentId);
    }

    @Test
    public void testGetPublishedArticleCommentsWithPagingAndSorting() throws Exception {
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(articleId);

        int page = 1;
        int size = 30;

        PageResult<CommentReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(commentService.getPublishedComments(articleId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/", articleId)
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
        dto.setTargetObjectType(TargetObjectType.ARTICLE);
        dto.setTargetObjectId(targetObjectId);
        return dto;
    }
}
