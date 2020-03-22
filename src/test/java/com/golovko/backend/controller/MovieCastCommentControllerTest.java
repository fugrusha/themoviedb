package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.BlockedUserException;
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
@WebMvcTest(MovieCastCommentController.class)
public class MovieCastCommentControllerTest {

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetMovieCastCommentById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCastId);

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
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCastId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getAllPublishedComments(movieCastId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        movieId, movieCastId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getAllPublishedComments(movieCastId);
    }

    @Test
    public void testGetMovieCastCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCastId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Comment.class, wrongId, movieCastId);

        Mockito.when(commentService.getComment(movieCastId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments/{id}",
                        movieId, movieCastId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        CommentReadDTO readDTO = createCommentReadDTO(authorId, movieCastId);

        Mockito.when(commentService.createComment(movieCastId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        movieId, movieCastId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultString, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).createComment(movieCastId, createDTO);
    }

    @Test
    public void testCreateMovieCastCommentBlockedUserException() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        BlockedUserException exception = new BlockedUserException(authorId);

        Mockito.when(commentService.createComment(movieCastId, createDTO)).thenThrow(exception);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments",
                        movieId, movieCastId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultString.contains(exception.getMessage()));
    }

    @Test
    public void testUpdateMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCastId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

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

    @Test
    public void testPatchMovieCastComment() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCastId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

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

    private CommentReadDTO createCommentReadDTO(UUID authorId, UUID targetObjectId) {
        CommentReadDTO dto = new CommentReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMessage("some text");
        dto.setAuthorId(authorId);
        dto.setTargetObjectType(TargetObjectType.MOVIE_CAST);
        dto.setTargetObjectId(targetObjectId);
        dto.setDislikesCount(46);
        dto.setLikesCount(120);
        dto.setStatus(CommentStatus.PENDING);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
