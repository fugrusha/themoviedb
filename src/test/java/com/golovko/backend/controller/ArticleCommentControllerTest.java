package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.ArticleCommentService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
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
    private ArticleCommentService articleCommentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getArticleCommentByIdTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        Mockito.when(articleCommentService.getComment(articleId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/{id}", articleId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(articleCommentService).getComment(articleId, readDTO.getId());
    }

    @Test
    public void getListOfArticleCommentsTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(articleCommentService.getAllComments(articleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/moderator", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(articleCommentService).getAllComments(articleId);
    }

    @Test
    public void getPublishedArticleCommentsTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(articleCommentService.getAllPublishedComments(articleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(articleCommentService).getAllPublishedComments(articleId);
    }

    @Test
    public void getArticleCommentByWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(articleCommentService.getComment(articleId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/articles/{articleId}/comments/{id}", articleId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Ignore // TODO add user authentication
    @Test
    public void createArticleCommentTest() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");

        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

//        Mockito.when(articleCommentService.createComment(articleId, createDTO, author)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/articles/{articleId}/comments/", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultString, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

//        Mockito.verify(articleService).createArticle(createDTO, author);
    }

    @Test
    public void updateArticleCommentTest() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        Mockito.when(articleCommentService.updateComment(articleId, readDTO.getId(), putDTO))
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
    public void patchArticleCommentTest() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, articleId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        Mockito.when(articleCommentService.patchComment(articleId, readDTO.getId(), patchDTO))
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
    public void deleteArticleCommentTest() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/articles/{articleId}/comments/{id}", articleId, commentId))
                .andExpect(status().isOk());

        Mockito.verify(articleCommentService).deleteComment(articleId, commentId);
    }

    private CommentReadDTO createCommentReadDTO(UUID authorId, UUID parentId) {
        CommentReadDTO dto = new CommentReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMessage("some text");
        dto.setAuthorId(authorId);
        dto.setParentId(parentId);
        dto.setDislikesCount(46);
        dto.setLikesCount(120);
        dto.setStatus(CommentStatus.PENDING);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
