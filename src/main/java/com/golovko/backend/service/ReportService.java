package com.golovko.backend.service;

import com.golovko.backend.domain.Report;
import com.golovko.backend.dto.ReportCreateDTO;
import com.golovko.backend.dto.ReportPatchDTO;
import com.golovko.backend.dto.ReportReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    private Report getReportRequired(UUID id) {
        return reportRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Report.class, id)
        );
    }

    private ReportReadDTO toRead(Report report) {
        ReportReadDTO dto = new ReportReadDTO();
        dto.setId(report.getId());
        dto.setReportTitle(report.getReportTitle());
        dto.setReportText(report.getReportText());
        dto.setReportType(report.getReportType());
        return dto;
    }

    public ReportReadDTO getReport(UUID id) {
        Report report = getReportRequired(id);
        return toRead(report);
    }


    public ReportReadDTO createReport(ReportCreateDTO createDTO) {
        Report report = new Report();
//        report.setIssueDate(Instant.now());
        report.setReportTitle(createDTO.getReportTitle());
        report.setReportText(createDTO.getReportText());
        report.setReportType(createDTO.getReportType());

        report = reportRepository.save(report);
        return toRead(report);
    }

    public ReportReadDTO patchReport(UUID id, ReportPatchDTO patchDTO) {
        Report report = getReportRequired(id);

        if (patchDTO.getReportTitle() != null) {
            report.setReportTitle(patchDTO.getReportTitle());
        }
        if (patchDTO.getReportText() != null) {
            report.setReportText(patchDTO.getReportText());
        }
        if (patchDTO.getReportType() != null) {
            report.setReportType(patchDTO.getReportType());
        }

        report = reportRepository.save(report);

        return toRead(report);
    }

    public void deleteReport(UUID id) {
        reportRepository.delete(getReportRequired(id));
    }
}
