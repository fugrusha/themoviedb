package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
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

@WebMvcTest(MisprintController.class)
public class MisprintControllerTest extends BaseControllerTest {

    @MockBean
    private MisprintService misprintService;

    @WithMockUser
    @Test
    public void testGetMisprintComplaintById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO(userId, moderatorId);

        Mockito.when(misprintService.getMisprintComplaint(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/misprints/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(misprintService).getMisprintComplaint(userId, readDTO.getId());
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsReportedByUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO(userId, moderatorId);
        MisprintReadDTO m2 = createMisprintReadDTO(userId, moderatorId);

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(m1, m2));

        Mockito.when(misprintService.getAllUserMisprintComplaints(userId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/misprints/", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(MisprintReadDTO::getId)
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());

        Mockito.verify(misprintService).getAllUserMisprintComplaints(userId, PageRequest.of(0, defaultPageSize));
    }

    @WithMockUser
    @Test
    public void testGetMisprintsWithPagingAndSorting() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO(userId, moderatorId);

        int page = 1;
        int size = 30;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(m1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(misprintService.getAllUserMisprintComplaints(userId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/misprints/", userId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
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

    @WithMockUser
    @Test
    public void testCreateMisprintComplaint() throws Exception {
        MisprintCreateDTO createDTO = new MisprintCreateDTO();
        createDTO.setMisprintText("misprint");
        createDTO.setReplaceTo("Text to replace");
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();

        MisprintReadDTO readDTO = createMisprintReadDTO(userId, moderatorId);

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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
    @Test
    public void testDeleteMisprintComplaint() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/misprints/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(misprintService).deleteMisprintComplaint(userId, id);
    }

    private MisprintReadDTO createMisprintReadDTO(UUID authorId, UUID moderatorId) {
        MisprintReadDTO dto = generateObject(MisprintReadDTO.class);
        dto.setAuthorId(authorId);
        dto.setModeratorId(moderatorId);
        return dto;
    }
}
