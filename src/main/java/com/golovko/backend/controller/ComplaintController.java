package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.CurrentUser;
import com.golovko.backend.controller.security.ModeratorOrCurrentUser;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/complaints")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @ApiOperation(value = "Get complaint by id", notes = "Needs MODERATOR or current user authority.")
    @ModeratorOrCurrentUser
    @GetMapping("/{id}")
    public ComplaintReadDTO getComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        return complaintService.getComplaint(userId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all user complaints", notes = "Needs MODERATOR or current user authority.")
    @ModeratorOrCurrentUser
    @GetMapping
    public PageResult<ComplaintReadDTO> getUserComplaints(
            @PathVariable UUID userId,
            @ApiIgnore Pageable pageable
    ) {
        return complaintService.getUserComplaints(userId, pageable);
    }

    @ApiOperation(value = "Create complaint")
    @PostMapping
    public ComplaintReadDTO createComplaint(
            @PathVariable UUID userId,
            @RequestBody @Valid ComplaintCreateDTO createDTO
    ) {
        return complaintService.createComplaint(userId, createDTO);
    }

    @ApiOperation(value = "Update user complaint",
            notes = "Needs current user authority. Empty fields will not be updated")
    @CurrentUser
    @PatchMapping("/{id}")
    public ComplaintReadDTO patchComplaint(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintPatchDTO patchDTO) {
        return complaintService.patchComplaint(userId, id, patchDTO);
    }

    @ApiOperation(value = "Update user complaint",
            notes = "Needs current user authority. All fields will be updated")
    @CurrentUser
    @PutMapping("/{id}")
    public ComplaintReadDTO updateComplaint(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintPutDTO updateDTO) {
        return complaintService.updateComplaint(userId, id, updateDTO);
    }

    @ApiOperation(value = "Delete user complaint", notes = "Needs current user authority")
    @CurrentUser
    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        complaintService.deleteComplaint(userId, id);
    }
}
