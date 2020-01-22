package com.golovko.backend.controller;

import com.golovko.backend.dto.ComplaintCreateDTO;
import com.golovko.backend.dto.ComplaintPatchDTO;
import com.golovko.backend.dto.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/{id}")
    public ComplaintReadDTO getReport(@PathVariable UUID id) {
        return complaintService.getReport(id);
    }

    @PostMapping
    public ComplaintReadDTO createReport(@RequestBody ComplaintCreateDTO createDTO) {
        return complaintService.createReport(createDTO);
    }
//
    @PatchMapping("/{id}")
    public ComplaintReadDTO patchReport(@PathVariable UUID id, @RequestBody ComplaintPatchDTO patchDTO) {
        return complaintService.patchReport(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteReport(@PathVariable UUID id) {
        complaintService.deleteReport(id);
    }

}
