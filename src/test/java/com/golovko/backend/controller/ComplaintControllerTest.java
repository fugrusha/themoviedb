package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.ComplaintService;
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
@WebMvcTest(ComplaintController.class)
public class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    @Test
    public void testGetComplaintById() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, parentId, moderatorId);

        Mockito.when(complaintService.getComplaint(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(complaintService).getComplaint(userId, readDTO.getId());
    }

    @Test
    public void testGetAllUserComplaints() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO c1 = createComplaintReadDTO(userId1, parentId, moderatorId);
        ComplaintReadDTO c2 = createComplaintReadDTO(userId1, parentId, moderatorId);

        List<ComplaintReadDTO> expectedResult = List.of(c1, c2);

        Mockito.when(complaintService.getUserComplaints(userId1)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/", userId1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(ComplaintReadDTO::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());

        Mockito.verify(complaintService).getUserComplaints(userId1);
    }

    @Test
    public void testGetComplaintByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(Movie.class, wrongId, userId);

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
        UUID targetObjectId = createDTO.getTargetObjectId();
        UUID moderatorId = UUID.randomUUID();

        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, targetObjectId, moderatorId);

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
    public void testPatchComplaint() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, parentId, moderatorId);

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
        Assert.assertEquals(readDTO, actualComplaint);
    }

    @Test
    public void testUpdateComplaint() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, parentId, moderatorId);

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
        Assert.assertEquals(readDTO, actualComplaint);
    }

    @Test
    public void testDeleteComplaint() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/complaints/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(complaintService).deleteComplaint(userId, id);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId, UUID parentId, UUID moderatorId) {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setComplaintStatus(ComplaintStatus.DUPLICATE);
        readDTO.setAuthorId(authorId);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        readDTO.setTargetObjectType(TargetObjectType.PERSON);
        readDTO.setTargetObjectId(parentId);
        readDTO.setModeratorId(moderatorId);
        return readDTO;
    }
}
