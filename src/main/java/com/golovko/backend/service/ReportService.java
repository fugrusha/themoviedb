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

    @Autowired
    private TranslationService translationService;

    public ReportReadDTO getReport(UUID id) {
        Report report = getReportRequired(id);
        return translationService.toRead(report);
    }

    public ReportReadDTO createReport(ReportCreateDTO createDTO) {
        Report report = translationService.toEntity(createDTO);

        report = reportRepository.save(report);
        return translationService.toRead(report);
    }

    public ReportReadDTO patchReport(UUID id, ReportPatchDTO patchDTO) {
        Report report = getReportRequired(id);

        translationService.patchEntity(patchDTO, report);

        report = reportRepository.save(report);

        return translationService.toRead(report);
    }

    public void deleteReport(UUID id) {
        reportRepository.delete(getReportRequired(id));
    }

    private Report getReportRequired(UUID id) {
        return reportRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Report.class, id)
        );
    }
}
