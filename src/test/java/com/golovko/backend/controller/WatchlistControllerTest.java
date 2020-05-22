package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.watchlist.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.WatchlistService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WatchlistController.class)
public class WatchlistControllerTest extends BaseControllerTest {

    @MockBean
    private WatchlistService watchlistService;

    @WithMockUser
    @Test
    public void testGetAllUserWatchlists() throws Exception {
        UUID userId = UUID.randomUUID();
        WatchlistReadDTO dto = generateObject(WatchlistReadDTO.class);
        List<WatchlistReadDTO> expectedResult = List.of(dto);

        Mockito.when(watchlistService.getAllUserWatchlists(userId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/watchlists", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<WatchlistReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});

        Assert.assertEquals(expectedResult, actualResult);
        Mockito.verify(watchlistService).getAllUserWatchlists(userId);
    }

    @WithMockUser
    @Test
    public void testGetUserWatchlist() throws Exception {
        UUID userId = UUID.randomUUID();
        WatchlistReadDTO readDTO = generateObject(WatchlistReadDTO.class);

        Mockito.when(watchlistService.getUserWatchlist(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/watchlists/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadDTO.class);

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);
        Mockito.verify(watchlistService).getUserWatchlist(userId, readDTO.getId());
    }

    @WithMockUser
    @Test
    public void testGetWatchlistWrongId() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);
        Mockito.when(watchlistService.getUserWatchlist(userId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/users/{userId}/watchlists/{id}", userId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @WithMockUser
    @Test
    public void testGetUserWatchlistExtended() throws Exception {
        UUID userId = UUID.randomUUID();
        WatchlistReadExtendedDTO readDTO = generateObject(WatchlistReadExtendedDTO.class);

        Mockito.when(watchlistService.getUserWatchlistExtended(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/watchlists/{id}/extended", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadExtendedDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadExtendedDTO.class);

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);
        Mockito.verify(watchlistService).getUserWatchlistExtended(userId, readDTO.getId());
    }

    @WithMockUser
    @Test
    public void testCreateWatchlist() throws Exception {
        UUID userId = UUID.randomUUID();
        WatchlistReadDTO readDTO = generateObject(WatchlistReadDTO.class);

        WatchlistCreateDTO createDTO = new WatchlistCreateDTO();
        createDTO.setName("watch later");

        Mockito.when(watchlistService.createWatchlist(userId, createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/watchlists/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadDTO.class);

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);
        Mockito.verify(watchlistService).createWatchlist(userId, createDTO);
    }

    @WithMockUser
    @Test
    public void testCreateWatchlistNotNullValidation() throws Exception {
        UUID userId = UUID.randomUUID();

        WatchlistCreateDTO createDTO = new WatchlistCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/watchlists/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).createWatchlist(any(), any());
    }

    @WithMockUser
    @Test
    public void testCreateWatchlistMinSizeValidation() throws Exception {
        UUID userId = UUID.randomUUID();

        WatchlistCreateDTO createDTO = new WatchlistCreateDTO();
        createDTO.setName("");

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/watchlists/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).createWatchlist(any(), any());
    }

    @WithMockUser
    @Test
    public void testCreateWatchlistMaxSizeValidation() throws Exception {
        UUID userId = UUID.randomUUID();

        WatchlistCreateDTO createDTO = new WatchlistCreateDTO();
        createDTO.setName("name".repeat(100));

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/watchlists/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).createWatchlist(any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateWatchlist() throws Exception {
        UUID userId = UUID.randomUUID();
        WatchlistReadDTO readDTO = generateObject(WatchlistReadDTO.class);

        WatchlistPutDTO putDTO = new WatchlistPutDTO();
        putDTO.setName("watch later");

        Mockito.when(watchlistService.updateWatchlist(userId, readDTO.getId(), putDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{userId}/watchlists/{id}", userId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadDTO.class);

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);
        Mockito.verify(watchlistService).updateWatchlist(userId, readDTO.getId(), putDTO);
    }

    @WithMockUser
    @Test
    public void testUpdateWatchlistNotNullValidation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();

        WatchlistPutDTO putDTO = new WatchlistPutDTO();

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{userId}/watchlists/{id}", userId, watchlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).updateWatchlist(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateWatchlistMinSizeValidation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();

        WatchlistPutDTO putDTO = new WatchlistPutDTO();
        putDTO.setName("");

        String resultJson = mockMvc
        .perform(put("/api/v1/users/{userId}/watchlists/{id}", userId, watchlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).updateWatchlist(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateWatchlistMaxSizeValidation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();

        WatchlistPutDTO putDTO = new WatchlistPutDTO();
        putDTO.setName("name".repeat(100));

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{userId}/watchlists/{id}", userId, watchlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(putDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).updateWatchlist(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchWatchlist() throws Exception {
        UUID userId = UUID.randomUUID();
        WatchlistReadDTO readDTO = generateObject(WatchlistReadDTO.class);

        WatchlistPatchDTO patchDTO = new WatchlistPatchDTO();
        patchDTO.setName("watch later");

        Mockito.when(watchlistService.patchWatchlist(userId, readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/watchlists/{id}", userId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadDTO.class);

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);
        Mockito.verify(watchlistService).patchWatchlist(userId, readDTO.getId(), patchDTO);
    }

    @WithMockUser
    @Test
    public void testPatchWatchlistNotNullValidation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();

        WatchlistPatchDTO patchDTO = new WatchlistPatchDTO();

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/watchlists/{id}", userId, watchlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).patchWatchlist(any(), any(), any());    }

    @WithMockUser
    @Test
    public void testPatchWatchlistMinSizeValidation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();

        WatchlistPatchDTO patchDTO = new WatchlistPatchDTO();
        patchDTO.setName("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/watchlists/{id}", userId, watchlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).patchWatchlist(any(), any(), any());    }

    @WithMockUser
    @Test
    public void testPatchWatchlistMaxSizeValidation() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();

        WatchlistPatchDTO patchDTO = new WatchlistPatchDTO();
        patchDTO.setName("name".repeat(100));

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/watchlists/{id}", userId, watchlistId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(watchlistService, Mockito.never()).patchWatchlist(any(), any(), any());
    }

    @WithMockUser
    @Test
    public void testDeleteWatchlist() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/watchlists/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(watchlistService).deleteWatchlist(userId, id);
    }

    @WithMockUser
    @Test
    public void testAddMovieToWatchlist() throws Exception {
        WatchlistReadDTO readDTO = generateObject(WatchlistReadDTO.class);
        UUID userId = readDTO.getAuthorId();
        UUID watchlistId = readDTO.getId();
        UUID movieId = UUID.randomUUID();

        Mockito.when(watchlistService.addMovieToWatchlist(userId, watchlistId, movieId)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/watchlists/{id}/movies/{movieId}",
                       userId, watchlistId, movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadDTO.class);
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);

        Mockito.verify(watchlistService).addMovieToWatchlist(userId, watchlistId, movieId);
    }

    @WithMockUser
    @Test
    public void testRemoveMovieFromWatchlist() throws Exception {
        WatchlistReadDTO readDTO = generateObject(WatchlistReadDTO.class);
        UUID userId = readDTO.getAuthorId();
        UUID watchlistId = readDTO.getId();
        UUID movieId = UUID.randomUUID();

        Mockito.when(watchlistService.removeMovieFromWatchlist(userId, watchlistId, movieId)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(delete("/api/v1/users/{userId}/watchlists/{id}/movies/{movieId}",
                        userId, watchlistId, movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        WatchlistReadDTO actualResult = objectMapper.readValue(resultJson, WatchlistReadDTO.class);
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(actualResult);

        Mockito.verify(watchlistService).removeMovieFromWatchlist(userId, watchlistId, movieId);
    }

    @WithMockUser
    @Test
    public void testDuplicatedMovieToWatchlist() throws Exception { ;
        UUID userId = UUID.randomUUID();
        UUID watchlistId = UUID.randomUUID();
        UUID movieId = UUID.randomUUID();

        LinkDuplicatedException ex = new LinkDuplicatedException(
                String.format("Watchlist %s already has movie %s", watchlistId, movieId));

        Mockito.when(watchlistService.addMovieToWatchlist(userId, watchlistId, movieId)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/watchlists/{id}/movies/{movieId}",
                        userId, watchlistId, movieId))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo actualResult = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(actualResult.getMessage(), ex.getMessage());

        Mockito.verify(watchlistService).addMovieToWatchlist(userId, watchlistId, movieId);
    }
}
