package com.golovko.backend.controller;

import com.golovko.backend.dto.misprint.MisprintCreateDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.service.MisprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
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
    public List<MisprintReadDTO> getAllReportedMisprintComplaints(@PathVariable UUID userId) {
        return misprintService.getAllUserMisprintComplaints(userId);
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
