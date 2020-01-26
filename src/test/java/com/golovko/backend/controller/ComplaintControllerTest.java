package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.complaint.ComplaintUpdateDTO;
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

    private ComplaintReadDTO createComplaintReadDTO() {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setAuthorId(UUID.randomUUID());
        return readDTO;
    }

    @Test
    public void getReportByIdTest() throws Exception {
        ComplaintReadDTO readDTO = createComplaintReadDTO();

        Mockito.when(complaintService.getComplaint(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(get("/api/v1/reports/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(complaintService).getComplaint(readDTO.getId());
    }

    @Test
    public void getReportByWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(complaintService.getComplaint(wrongId)).thenThrow(exception);

        String result = mockMvc.perform(get("/api/v1/reports/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void createReportTest() throws Exception {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Report 1");
        createDTO.setComplaintText("I have noticed a spoiler");
        createDTO.setComplaintType(ComplaintType.SPOILER);

        ComplaintReadDTO readDTO = createComplaintReadDTO();

        Mockito.when(complaintService.createComplaint(createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/reports")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchReportTest() throws Exception {
        ComplaintReadDTO readDTO = createComplaintReadDTO();

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        Mockito.when(complaintService.patchComplaint(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(patch("/api/v1/reports/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assert.assertEquals(readDTO, actualComplaint);
    }

    @Test
    public void updateComplaintTest() throws Exception {
        ComplaintReadDTO readDTO = createComplaintReadDTO();

        ComplaintUpdateDTO updateDTO = new ComplaintUpdateDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        Mockito.when(complaintService.updateComplaint(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(put("/api/v1/reports/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assert.assertEquals(readDTO, actualComplaint);
    }

    @Test
    public void deleteReportTest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/reports/{id}", id.toString()))
                .andExpect(status().isOk());

        Mockito.verify(complaintService).deleteComplaint(id);
    }
}
