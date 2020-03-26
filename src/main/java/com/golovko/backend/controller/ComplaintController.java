package com.golovko.backend.controller;

import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/{id}")
    public ComplaintReadDTO getComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        return complaintService.getComplaint(userId, id);
    }

    @GetMapping
    public List<ComplaintReadDTO> getAllUserComplaints(@PathVariable UUID userId) {
        return complaintService.getUserComplaints(userId);
    }

    @PostMapping
    public ComplaintReadDTO createComplaint(
            @PathVariable UUID userId,
            @RequestBody @Valid ComplaintCreateDTO createDTO
    ) {
        return complaintService.createComplaint(userId, createDTO);
    }

    @PatchMapping("/{id}")
    public ComplaintReadDTO patchComplaint(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintPatchDTO patchDTO) {
        return complaintService.patchComplaint(userId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public ComplaintReadDTO updateComplaint(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintPutDTO updateDTO) {
        return complaintService.updateComplaint(userId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        complaintService.deleteComplaint(userId, id);
    }
}
