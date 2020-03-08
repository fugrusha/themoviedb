package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.CommentService;
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
@WebMvcTest(ArticleCommentController.class)
public class ArticleCommentControllerTest {

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetArticleCommentById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

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
    public void testGetAllArticleComments() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getAllComments(articleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/moderator", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getAllComments(articleId);
    }

    @Test
    public void testGetAllPublishedArticleComments() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getAllPublishedComments(articleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getAllPublishedComments(articleId);
    }

    @Test
    public void testGetArticleCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

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
        UUID authorId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.ARTICLE);

        CommentReadDTO readDTO = createCommentReadDTO(authorId, articleId);

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
    public void testUpdateArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

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
    public void testPatchArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

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
    public void testDeleteArticleComment() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/articles/{articleId}/comments/{id}", articleId, commentId))
                .andExpect(status().isOk());

        Mockito.verify(commentService).deleteComment(articleId, commentId);
    }

    private CommentReadDTO createCommentReadDTO(UUID authorId, UUID targetObjectId) {
        CommentReadDTO dto = new CommentReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMessage("some text");
        dto.setAuthorId(authorId);
        dto.setTargetObjectType(TargetObjectType.ARTICLE);
        dto.setTargetObjectId(targetObjectId);
        dto.setDislikesCount(46);
        dto.setLikesCount(120);
        dto.setStatus(CommentStatus.PENDING);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
