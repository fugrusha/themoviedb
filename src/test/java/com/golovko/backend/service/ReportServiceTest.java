package com.golovko.backend.service;

import com.golovko.backend.domain.Report;
import com.golovko.backend.domain.ReportType;
import com.golovko.backend.dto.ReportCreateDTO;
import com.golovko.backend.dto.ReportPatchDTO;
import com.golovko.backend.dto.ReportReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ReportRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = "delete from report", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ReportServiceTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ReportRepository reportRepository;

    private Report createReport() {
        Report report = new Report();
        report.setReportTitle("Some title");
        report.setReportText("Some report text");
        report.setReportType(ReportType.SPOILER);
        report = reportRepository.save(report);
        return report;
    }

    @Test
    public void getReportTest() {
        Report report = createReport();
        ReportReadDTO readDTO = reportService.getReport(report.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(report);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getReportWrongIdTest() {
        reportService.getReport(UUID.randomUUID());
    }

    @Test
    public void createReportTest() {
        ReportCreateDTO createDTO = new ReportCreateDTO();
        createDTO.setReportTitle("some title");
        createDTO.setReportText("some text");
        createDTO.setReportType(ReportType.SPOILER);

        ReportReadDTO readDTO = reportService.createReport(createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Report report = reportRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(report);
    }

    @Test
    public void patchReportTest() {
        Report report = createReport();

        ReportPatchDTO patchDTO = new ReportPatchDTO();
        patchDTO.setReportTitle("another title");
        patchDTO.setReportText("another text");
        patchDTO.setReportType(ReportType.CHILD_ABUSE);

        ReportReadDTO readDTO = reportService.patchReport(report.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        report = reportRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(report).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchReportEmptyPatchTest() {
        Report report = createReport();

        ReportPatchDTO patchDTO = new ReportPatchDTO();
        ReportReadDTO readDTO = reportService.patchReport(report.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getReportTitle());
        Assert.assertNotNull(readDTO.getReportText());
        Assert.assertNotNull(readDTO.getReportType());

        Report reportAfterUpdate = reportRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(reportAfterUpdate.getReportTitle());
        Assert.assertNotNull(reportAfterUpdate.getReportText());
        Assert.assertNotNull(reportAfterUpdate.getReportType());

        Assertions.assertThat(report).isEqualToComparingFieldByField(reportAfterUpdate);
    }

    @Test
    public void deleteReportTest() {
        Report report = createReport();
        reportService.deleteReport(report.getId());

        Assert.assertFalse(reportRepository.existsById(report.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteReportNotFound() {
        reportService.deleteReport(UUID.randomUUID());
    }
}
