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
@WebMvcTest(MovieCrewCommentController.class)
public class MovieCrewCommentControllerTest {

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetMovieCrewCommentById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCrewId);

        Mockito.when(commentService.getComment(movieCrewId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        movieId, movieCrewId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).getComment(movieCrewId, readDTO.getId());
    }

    @Test
    public void testGetAllMovieCrewComments() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCrewId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getAllComments(movieCrewId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/moderator",
                        movieId, movieCrewId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getAllComments(movieCrewId);
    }

    @Test
    public void testGetAllPublishedMovieCrewComments() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCrewId);

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getAllPublishedComments(movieCrewId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
                        movieId, movieCrewId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(CommentReadDTO::getId)
                .containsExactlyInAnyOrder(readDTO.getId());

        Mockito.verify(commentService).getAllPublishedComments(movieCrewId);
    }

    @Test
    public void testGetMovieCrewCommentByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID movieCrewId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(commentService.getComment(movieCrewId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        movieId, movieCrewId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateMovieCrewComment() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(authorId);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE_CAST);

        CommentReadDTO readDTO = createCommentReadDTO(authorId, movieCrewId);

        Mockito.when(commentService.createComment(movieCrewId, createDTO)).thenReturn(readDTO);

        String resultString = mockMvc
                .perform(post("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments",
                        movieId, movieCrewId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultString, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(commentService).createComment(movieCrewId, createDTO);
    }

    @Test
    public void testUpdateMovieCrewComment() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCrewId);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        Mockito.when(commentService.updateComment(movieCrewId, readDTO.getId(), putDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        movieId, movieCrewId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchMovieCrewComment() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentReadDTO readDTO = createCommentReadDTO(userId, movieCrewId);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        Mockito.when(commentService.patchComment(movieCrewId, readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments/{id}",
                        movieId, movieCrewId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

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
