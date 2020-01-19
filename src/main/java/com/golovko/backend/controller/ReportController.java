package com.golovko.backend.controller;

import com.golovko.backend.dto.ReportCreateDTO;
import com.golovko.backend.dto.ReportPatchDTO;
import com.golovko.backend.dto.ReportReadDTO;
import com.golovko.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/{id}")
    public ReportReadDTO getReport(@PathVariable UUID id) {
        return reportService.getReport(id);
    }

    @PostMapping
    public ReportReadDTO createReport(@RequestBody ReportCreateDTO createDTO) {
        return reportService.createReport(createDTO);
    }
//
    @PatchMapping("/{id}")
    public ReportReadDTO patchReport(@PathVariable UUID id, @RequestBody ReportPatchDTO patchDTO) {
        return reportService.patchReport(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteReport(@PathVariable UUID id) {
        reportService.deleteReport(id);
    }

}
