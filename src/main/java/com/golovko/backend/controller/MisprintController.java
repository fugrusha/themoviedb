package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.misprint.MisprintCreateDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.service.MisprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/misprints")
public class MisprintController {

    @Autowired
    private MisprintService misprintService;

    @GetMapping("/{id}")
    public MisprintReadDTO getMisprintComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        return misprintService.getMisprintComplaint(userId, id);
    }

    @GetMapping
    public PageResult<MisprintReadDTO> getAllReportedMisprintComplaints(
            @PathVariable UUID userId,
            Pageable pageable
    ) {
        return misprintService.getAllUserMisprintComplaints(userId, pageable);
    }

    @PostMapping
    public MisprintReadDTO createMisprintComplaint(
            @PathVariable UUID userId,
            @RequestBody @Valid MisprintCreateDTO createDTO
    ) {
        return misprintService.createMisprintComplaint(userId, createDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMisprintComplaint(@PathVariable UUID userId, @PathVariable UUID id) {
        misprintService.deleteMisprintComplaint(userId, id);
    }
}
