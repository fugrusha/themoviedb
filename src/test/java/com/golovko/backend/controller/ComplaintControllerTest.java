package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ComplaintService;
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

@WebMvcTest(ComplaintController.class)
public class ComplaintControllerTest extends BaseControllerTest {

    @MockBean
    private ComplaintService complaintService;

    @Test
    public void testGetComplaintById() throws Exception {
        UUID userId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId);

        Mockito.when(complaintService.getComplaint(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(complaintService).getComplaint(userId, readDTO.getId());
    }

    @Test
    public void testGetAllUserComplaints() throws Exception {
        UUID userId = UUID.randomUUID();

        ComplaintReadDTO c1 = createComplaintReadDTO(userId);
        ComplaintReadDTO c2 = createComplaintReadDTO(userId);

        PageResult<ComplaintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(c1, c2));

        Mockito.when(complaintService.getUserComplaints(userId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(ComplaintReadDTO::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());

        Mockito.verify(complaintService).getUserComplaints(userId, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetUserComplaintsWithPagingAndSorting() throws Exception {
        UUID userId = UUID.randomUUID();
        ComplaintReadDTO c1 = createComplaintReadDTO(userId);

        int page = 1;
        int size = 30;

        PageResult<ComplaintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(c1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(complaintService.getUserComplaints(userId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/", userId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetComplaintByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(Complaint.class, wrongId, userId);

        Mockito.when(complaintService.getComplaint(userId, wrongId)).thenThrow(ex);

        String result = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/{id}", userId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(ex.getMessage()));
    }

    @Test
    public void testCreateComplaint() throws Exception {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        UUID userId = UUID.randomUUID();

        ComplaintReadDTO readDTO = createComplaintReadDTO(userId);

        Mockito.when(complaintService.createComplaint(userId, createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/complaints/", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testCreateComplaintNotNullValidationException() throws Exception {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/complaints/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).createComplaint(any(), any());
    }

    @Test
    public void testCreateComplaintMinSizeValidationException() throws Exception {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("");
        createDTO.setComplaintText("");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/complaints/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).createComplaint(any(), any());
    }

    @Test
    public void testCreateComplaintMaxSizeValidationException() throws Exception {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("complaint title".repeat(100));
        createDTO.setComplaintText("complaint long text".repeat(100));
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(UUID.randomUUID());

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/complaints/", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).createComplaint(any(), any());
    }

    @Test
    public void testPatchComplaint() throws Exception {
        UUID userId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        Mockito.when(complaintService.patchComplaint(userId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/complaints/{id}", userId, readDTO.getId())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchComplaintMinSizeValidationException() throws Exception {
        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("");
        patchDTO.setComplaintText("");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/complaints/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).patchComplaint(any(), any(), any());
    }

    @Test
    public void testPatchComplaintMaxSizeValidationException() throws Exception {
        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("complaint title".repeat(100));
        patchDTO.setComplaintText("complaint long text".repeat(100));
        patchDTO.setComplaintType(ComplaintType.SPAM);

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{userId}/complaints/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).patchComplaint(any(), any(), any());
    }

    @Test
    public void testUpdateComplaint() throws Exception {
        UUID userId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        Mockito.when(complaintService.updateComplaint(userId, readDTO.getId(), updateDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{userId}/complaints/{id}", userId, readDTO.getId())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testUpdateComplaintMinSizeValidationException() throws Exception {
        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintTitle("");
        updateDTO.setComplaintText("");
        updateDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{userId}/complaints/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).updateComplaint(any(), any(), any());
    }

    @Test
    public void testUpdateComplaintMaxSizeValidationException() throws Exception {
        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintTitle("complaint title".repeat(100));
        updateDTO.setComplaintText("complaint long text".repeat(100));
        updateDTO.setComplaintType(ComplaintType.SPAM);

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{userId}/complaints/{id}",
                        UUID.randomUUID(), UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).updateComplaint(any(), any(), any());
    }

    @Test
    public void testDeleteComplaint() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/complaints/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(complaintService).deleteComplaint(userId, id);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId) {
        ComplaintReadDTO readDTO = generateObject(ComplaintReadDTO.class);
        readDTO.setAuthorId(authorId);
        return readDTO;
    }
}
