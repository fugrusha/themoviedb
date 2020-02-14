package com.golovko.backend.controller;

import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.UserComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/complaints")
public class UserComplaintController {

    @Autowired
    private UserComplaintService userComplaintService;

    @GetMapping("/{id}")
    public ComplaintReadDTO getComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        return userComplaintService.getComplaint(userId, id);
    }

    @GetMapping
    public List<ComplaintReadDTO> getListOfUserComplaints(@PathVariable UUID userId) {
        return userComplaintService.getUserComplaints(userId);
    }

    @PatchMapping("/{id}")
    public ComplaintReadDTO patchComplaint(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody ComplaintPatchDTO patchDTO) {
        return userComplaintService.patchComplaint(userId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public ComplaintReadDTO updateComplaint(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody ComplaintPutDTO updateDTO) {
        return userComplaintService.updateComplaint(userId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        userComplaintService.deleteComplaint(userId, id);
    }
}
