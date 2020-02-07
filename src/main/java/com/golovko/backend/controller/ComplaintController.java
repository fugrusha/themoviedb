package com.golovko.backend.controller;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/{id}")
    public ComplaintReadDTO getComplaint(@PathVariable UUID id) {
        return complaintService.getComplaint(id);
    }

    // TODO add list of user's complaints

    @PostMapping
    public ComplaintReadDTO createComplaint(@RequestBody ComplaintCreateDTO createDTO, ApplicationUser author) {
        return complaintService.createComplaint(createDTO, author);
    }

    @PatchMapping("/{id}")
    public ComplaintReadDTO patchComplaint(@PathVariable UUID id, @RequestBody ComplaintPatchDTO patchDTO) {
        return complaintService.patchComplaint(id, patchDTO);
    }

    @PutMapping("/{id}")
    public ComplaintReadDTO updateComplaint(@PathVariable UUID id, @RequestBody ComplaintPutDTO updateDTO) {
        return complaintService.updateComplaint(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable UUID id) {
        complaintService.deleteComplaint(id);
    }

}
