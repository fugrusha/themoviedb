package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(complaintService).getAllComplaints(filter);
    }
}
