package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.misprint.MisprintCreateDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.MisprintService;
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

@WebMvcTest(MisprintController.class)
public class MisprintControllerTest extends BaseControllerTest {

    @MockBean
    private MisprintService misprintService;

    @Test
    public void testGetMisprintComplaintById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO(userId, parentId, moderatorId);

        Mockito.when(misprintService.getMisprintComplaint(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/misprints/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(misprintService).getMisprintComplaint(userId, readDTO.getId());
    }

    @Test
    public void testGetAllMisprintsReportedByUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        MisprintReadDTO m1 = createMistakeReadDTO(userId, parentId, moderatorId);
        MisprintReadDTO m2 = createMistakeReadDTO(userId, parentId, moderatorId);

        List<MisprintReadDTO> expectedResult = List.of(m1, m2);

        Mockito.when(misprintService.getAllUserMisprintComplaints(userId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/misprints/", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(MisprintReadDTO::getId)
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());

        Mockito.verify(misprintService).getAllUserMisprintComplaints(userId);
    }

    @Test
    public void testGetMisprintComplaintByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(Misprint.class, wrongId, userId);

        Mockito.when(misprintService.getMisprintComplaint(userId, wrongId)).thenThrow(ex);

        String result = mockMvc
                .perform(get("/api/v1/users/{userId}/misprints/{id}", userId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(ex.getMessage()));
    }

    @Test
    public void testCreateMisprintComplaint() throws Exception {
        MisprintCreateDTO createDTO = new MisprintCreateDTO();
        createDTO.setMisprintText("misprint");
        createDTO.setReplaceTo("Text to replace");
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        UUID userId = UUID.randomUUID();
        UUID targetObjectId = createDTO.getTargetObjectId();
        UUID moderatorId = UUID.randomUUID();

        MisprintReadDTO readDTO = createMistakeReadDTO(userId, targetObjectId, moderatorId);

        Mockito.when(misprintService.createMisprintComplaint(userId, createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/misprints/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testCreateMisprintNotNullValidationException() throws Exception {
        MisprintCreateDTO createDTO = new MisprintCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/misprints/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).createMisprintComplaint(any(), any());
    }

    @Test
    public void testCreateMisprintMinSizeValidationException() throws Exception {
        MisprintCreateDTO createDTO = new MisprintCreateDTO();
        createDTO.setMisprintText("");
        createDTO.setReplaceTo("");
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/misprints/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).createMisprintComplaint(any(), any());
    }

    @Test
    public void testCreateMisprintMaxSizeValidationException() throws Exception {
        MisprintCreateDTO createDTO = new MisprintCreateDTO();
        createDTO.setMisprintText("text with misprint".repeat(100));
        createDTO.setReplaceTo("new text".repeat(100));
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/misprints/", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).createMisprintComplaint(any(), any());
    }

    @Test
    public void testDeleteMisprintComplaint() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/misprints/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(misprintService).deleteMisprintComplaint(userId, id);
    }

    private MisprintReadDTO createMistakeReadDTO(UUID authorId, UUID parentId, UUID moderatorId) {
        MisprintReadDTO dto = new MisprintReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMisprintText("misprint");
        dto.setReplaceTo("replace to this");
        dto.setStatus(ComplaintStatus.INITIATED);
        dto.setAuthorId(authorId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        dto.setTargetObjectType(TargetObjectType.MOVIE);
        dto.setTargetObjectId(parentId);
        dto.setModeratorId(moderatorId);
        return dto;
    }
}
