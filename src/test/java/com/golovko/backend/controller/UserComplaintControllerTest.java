package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.UserComplaintService;
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
@WebMvcTest(UserComplaintController.class)
public class UserComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserComplaintService userComplaintService;

    @Test
    public void getComplaintByIdTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, parentId);

        Mockito.when(userComplaintService.getComplaint(userId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/{id}", userId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(userComplaintService).getComplaint(userId, readDTO.getId());
    }

    @Test
    public void getListOfUserComplaintsTest() throws Exception {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO c1 = createComplaintReadDTO(userId1, parentId);
        ComplaintReadDTO c2 = createComplaintReadDTO(userId1, parentId);
        createComplaintReadDTO(userId2, parentId);
        createComplaintReadDTO(userId2, parentId);

        List<ComplaintReadDTO> expectedResult = List.of(c1, c2);

        Mockito.when(userComplaintService.getUserComplaints(userId1)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/", userId1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(ComplaintReadDTO::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());

        Mockito.verify(userComplaintService).getUserComplaints(userId1);
    }

    @Test
    public void getComplaintByWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(userComplaintService.getComplaint(userId, wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/users/{userId}/complaints/{id}", userId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void patchComplaintTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, parentId);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);
        patchDTO.setComplaintStatus(ComplaintStatus.RENEWED);

        Mockito.when(userComplaintService.patchComplaint(userId, readDTO.getId(), patchDTO))
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
    public void updateComplaintTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, parentId);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(ComplaintType.CHILD_ABUSE);
        updateDTO.setComplaintStatus(ComplaintStatus.CLOSED);

        Mockito.when(userComplaintService.updateComplaint(userId, readDTO.getId(), updateDTO))
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
    public void deleteComplaintTest() throws Exception {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{userId}/complaints/{id}", userId, id))
                .andExpect(status().isOk());

        Mockito.verify(userComplaintService).deleteComplaint(userId, id);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId, UUID parentId) {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setComplaintStatus(ComplaintStatus.DUPLICATE);
        readDTO.setAuthorId(authorId);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        readDTO.setParentId(parentId);
        return readDTO;
    }
}
