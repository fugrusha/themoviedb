package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.CurrentUser;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.misprint.MisprintCreateDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.service.MisprintService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/misprints")
public class MisprintController {

    @Autowired
    private MisprintService misprintService;

    @ApiOperation(value = "Get misprint", notes = "Needs current user authority")
    @CurrentUser
    @GetMapping("/{id}")
    public MisprintReadDTO getMisprintComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        return misprintService.getMisprintComplaint(userId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints created by user", notes = "Needs current user authority")
    @CurrentUser
    @GetMapping
    public PageResult<MisprintReadDTO> getAllReportedMisprintComplaints(
            @PathVariable UUID userId,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getAllUserMisprintComplaints(userId, pageable);
    }

    @ApiOperation(value = "Create misprint")
    @PostMapping
    public MisprintReadDTO createMisprintComplaint(
            @PathVariable UUID userId,
            @RequestBody @Valid MisprintCreateDTO createDTO
    ) {
        return misprintService.createMisprintComplaint(userId, createDTO);
    }

    @ApiOperation(value = "Delete misprint", notes = "Needs current user authority")
    @CurrentUser
    @DeleteMapping("/{id}")
    public void deleteMisprintComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        misprintService.deleteMisprintComplaint(userId, id);
    }
}
