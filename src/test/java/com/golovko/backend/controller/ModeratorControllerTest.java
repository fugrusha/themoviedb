package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.moderator.ModeratorDTO;
import com.golovko.backend.service.ComplaintService;
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
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ModeratorController.class)
public class ModeratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    @Test
    public void testGetComplaintsWithFilter() throws Exception {
        ComplaintFilter filter = new ComplaintFilter();
        filter.setAuthorId(UUID.randomUUID());
        filter.setModeratorId(UUID.randomUUID());
        filter.setComplaintTypes(Set.of(ComplaintType.VIOLENCE, ComplaintType.SPAM));
        filter.setStatuses(Set.of(ComplaintStatus.INITIATED));
        filter.setTargetObjectTypes(Set.of(TargetObjectType.COMMENT));

        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setModeratorId(filter.getModeratorId());
        readDTO.setAuthorId(filter.getAuthorId());
        readDTO.setComplaintType(ComplaintType.SPAM);
        readDTO.setComplaintStatus(ComplaintStatus.INITIATED);
        readDTO.setTargetObjectType(TargetObjectType.COMMENT);
        readDTO.setTargetObjectId(UUID.randomUUID());
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("title");
        readDTO.setComplaintText("description");

        List<ComplaintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(complaintService.getAllComplaints(filter)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/moderator/complaints")
                .param("moderatorId", filter.getModeratorId().toString())
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "INITIATED")
                .param("complaintTypes", "VIOLENCE, SPAM")
                .param("targetObjectTypes", "COMMENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(complaintService).getAllComplaints(filter);
    }

    @Ignore
    @Test
    public void testTakeComplaintForModeration() throws Exception {
        ModeratorDTO moderDTO = new ModeratorDTO();
        moderDTO.setModeratorId(UUID.randomUUID());

        UUID userId = UUID.randomUUID();
        ComplaintReadDTO complaintDTO = createComplaintReadDTO(userId, moderDTO.getModeratorId());

        Mockito.when(complaintService.takeForModeration(complaintDTO.getId(), moderDTO))
                .thenReturn(complaintDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/moderator/complaints/{id}/moderate", complaintDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moderDTO)))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualResult = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(complaintDTO);

        Mockito.verify(complaintService).takeForModeration(complaintDTO.getId(), moderDTO);
    }

    @Ignore
    @Test
    public void testChangeComplaintStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, moderatorId);

        ModeratorDTO moderatorDTO = new ModeratorDTO();
        moderatorDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);

        Mockito.when(complaintService.changeStatus(readDTO.getId(), moderatorDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/moderator/complaints/{id}/change-status", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moderatorDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualResult = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(complaintService).changeStatus(readDTO.getId(), moderatorDTO);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId, UUID moderatorId) {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setComplaintStatus(ComplaintStatus.INITIATED);
        readDTO.setAuthorId(authorId);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        readDTO.setTargetObjectType(TargetObjectType.PERSON);
        readDTO.setTargetObjectId(UUID.randomUUID());
        readDTO.setModeratorId(moderatorId);
        return readDTO;
    }
}
