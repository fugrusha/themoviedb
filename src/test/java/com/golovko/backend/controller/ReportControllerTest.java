package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.ReportType;
import com.golovko.backend.dto.ReportCreateDTO;
import com.golovko.backend.dto.ReportPatchDTO;
import com.golovko.backend.dto.ReportReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.ReportService;
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
@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    private ReportReadDTO createReportReadDTO() {
        ReportReadDTO readDTO = new ReportReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setReportTitle("Report 1");
        readDTO.setReportText("I have noticed a spoiler");
        readDTO.setReportType(ReportType.SPOILER);
        return readDTO;
    }

    @Test
    public void getReportByIdTest() throws Exception {
        ReportReadDTO readDTO = createReportReadDTO();

        Mockito.when(reportService.getReport(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(get("/api/v1/reports/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ReportReadDTO actualReport = objectMapper.readValue(resultJson, ReportReadDTO.class);
        Assertions.assertThat(actualReport).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(reportService).getReport(readDTO.getId());
    }

    @Test
    public void getReportByWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);
        Mockito.when(reportService.getReport(wrongId)).thenThrow(exception);

        String result = mockMvc.perform(get("/api/v1/reports/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void createReportTest() throws Exception {
        ReportCreateDTO createDTO = new ReportCreateDTO();
        createDTO.setReportTitle("Report 1");
        createDTO.setReportText("I have noticed a spoiler");
        createDTO.setReportType(ReportType.SPOILER);

        ReportReadDTO readDTO = createReportReadDTO();

        Mockito.when(reportService.createReport(createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/reports")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ReportReadDTO actualReport = objectMapper.readValue(resultJson, ReportReadDTO.class);
        Assertions.assertThat(actualReport).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchReportTest() throws Exception {
        ReportReadDTO readDTO = createReportReadDTO();

        ReportPatchDTO patchDTO = new ReportPatchDTO();
        patchDTO.setReportTitle("another title");
        patchDTO.setReportText("another text");
        patchDTO.setReportType(ReportType.CHILD_ABUSE);

        Mockito.when(reportService.patchReport(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc.perform(patch("/api/v1/reports/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ReportReadDTO actualReport = objectMapper.readValue(resultJson, ReportReadDTO.class);
        Assert.assertEquals(readDTO, actualReport);
    }

    @Test
    public void deleteReportTest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/reports/{id}", id.toString()))
                .andExpect(status().isOk());

        Mockito.verify(reportService).deleteReport(id);
    }
}
