package com.golovko.backend.controller;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.MovieComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/complaints")
public class MovieComplaintController {

    @Autowired
    private MovieComplaintService movieComplaintService;

    @GetMapping("/{id}")
    public ComplaintReadDTO getComplaint(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieComplaintService.getMovieComplaint(movieId, id);
    }

    @GetMapping
    public List<ComplaintReadDTO> getMovieComplaints(@PathVariable UUID movieId) {
        return movieComplaintService.getMovieComplaints(movieId);
    }

    @PostMapping
    public ComplaintReadDTO createMovieComplaint(
            @PathVariable UUID movieId,
            @RequestBody ComplaintCreateDTO createDTO,
            ApplicationUser author
    ) {
        return movieComplaintService.createMovieComplaint(movieId, createDTO, author);
    }

    @PatchMapping("/{id}")
    public ComplaintReadDTO patchComplaint(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody ComplaintPatchDTO patchDTO) {
        return movieComplaintService.patchMovieComplaint(movieId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public ComplaintReadDTO updateComplaint(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody ComplaintPutDTO updateDTO) {
        return movieComplaintService.updateMovieComplaint(movieId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable UUID movieId, @PathVariable UUID id) {
        movieComplaintService.deleteMovieComplaint(movieId, id);
    }
}
