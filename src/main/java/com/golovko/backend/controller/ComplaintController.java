package com.golovko.backend.controller;

import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
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
        return complaintService.getComplaint(id);
    }

    @PostMapping
    public ComplaintReadDTO createReport(@RequestBody ComplaintCreateDTO createDTO) {
        return complaintService.createComplaint(createDTO);
    }

    @PatchMapping("/{id}")
    public ComplaintReadDTO patchReport(@PathVariable UUID id, @RequestBody ComplaintPatchDTO patchDTO) {
        return complaintService.patchComplaint(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteReport(@PathVariable UUID id) {
        complaintService.deleteComplaint(id);
    }

}
