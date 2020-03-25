package com.golovko.backend.controller;

import com.golovko.backend.domain.ActionType;
import com.golovko.backend.domain.Like;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.WrongTypeOfTargetObjectException;
import com.golovko.backend.service.LikeService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LikeController.class)
public class LikeControllerTest extends BaseControllerTest {

    @MockBean
    private LikeService likeService;

    @Test
    public void testGetLikeById() throws Exception {
        UUID userId = UUID.randomUUID();
        LikeReadDTO readDTO = createLikeReadDTO(userId, true);

        Mockito.when(likeService.getLike(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJSON = mockMvc
                .perform(get("/api/v1/users/{userId}/likes/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LikeReadDTO actualResult = objectMapper.readValue(resultJSON, LikeReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(likeService).getLike(userId, readDTO.getId());
    }

    @Test
    public void testGetLikeWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Like.class, wrongId);
        Mockito.when(likeService.getLike(userId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/users/{userId}/likes/{id}", userId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testCreateLike() throws Exception {
        UUID userId = UUID.randomUUID();
        LikeReadDTO readDTO = createLikeReadDTO(userId, true);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setLikedObjectId(readDTO.getLikedObjectId());
        createDTO.setLikedObjectType(readDTO.getLikedObjectType());
        createDTO.setMeLiked(readDTO.getMeLiked());

        Mockito.when(likeService.createLike(userId, createDTO)).thenReturn(readDTO);

        String resultJSON = mockMvc
                .perform(post("/api/v1/users/{userId}/likes/", userId, createDTO)
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LikeReadDTO actualResult = objectMapper.readValue(resultJSON, LikeReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testCreateLikeWrongTypeOfTargetObjectException() throws Exception {
        UUID userId = UUID.randomUUID();

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setLikedObjectId(UUID.randomUUID());
        createDTO.setLikedObjectType(TargetObjectType.MOVIE_CAST);
        createDTO.setMeLiked(true);

        WrongTypeOfTargetObjectException exception =
                new WrongTypeOfTargetObjectException(ActionType.ADD_LIKE, createDTO.getLikedObjectType());

        Mockito.when(likeService.createLike(userId, createDTO)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/likes/", userId, createDTO)
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void testPatchLike() throws Exception {
        UUID userId = UUID.randomUUID();
        LikeReadDTO readDTO = createLikeReadDTO(userId, true);

        LikePatchDTO patchDTO = new LikePatchDTO();
        patchDTO.setMeLiked(false);

        Mockito.when(likeService.patchLike(userId, readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJSON = mockMvc
                .perform(patch("/api/v1/users/{userId}/likes/{id}",
                        userId, readDTO.getId(), patchDTO)
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LikeReadDTO actualResult = objectMapper.readValue(resultJSON, LikeReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testUpdateLike() throws Exception {
        UUID userId = UUID.randomUUID();
        LikeReadDTO readDTO = createLikeReadDTO(userId, true);

        LikePutDTO putDTO = new LikePutDTO();
        putDTO.setMeLiked(false);

        Mockito.when(likeService.updateLike(userId, readDTO.getId(), putDTO)).thenReturn(readDTO);

        String resultJSON = mockMvc
                .perform(put("/api/v1/users/{userId}/likes/{id}",
                        userId, readDTO.getId(), putDTO)
                .content(objectMapper.writeValueAsString(putDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        LikeReadDTO actualResult = objectMapper.readValue(resultJSON, LikeReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testDeleteLike() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/likes/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(likeService).deleteLike(userId, id);
    }

    private LikeReadDTO createLikeReadDTO(UUID authorId, Boolean meLiked) {
        LikeReadDTO dto = new LikeReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMeLiked(meLiked);
        dto.setAuthorId(authorId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        dto.setLikedObjectType(TargetObjectType.MOVIE);
        dto.setLikedObjectId(UUID.randomUUID());
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
