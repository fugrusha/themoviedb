package com.golovko.backend.controller;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ArticleComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/articles/{articleId}/complaints")
public class ArticleComplaintController {

    @Autowired
    private ArticleComplaintService articleComplaintService;

    @GetMapping("/{id}")
    public ComplaintReadDTO getComplaint(@PathVariable UUID articleId, @PathVariable UUID id) {
        return articleComplaintService.getComplaint(articleId, id);
    }

    @GetMapping
    public List<ComplaintReadDTO> getAllComplaints(@PathVariable UUID articleId) {
        return articleComplaintService.getAllComplaints(articleId);
    }

    @PostMapping
    public ComplaintReadDTO createComplaint(
            @PathVariable UUID articleId,
            @RequestBody ComplaintCreateDTO createDTO,
            ApplicationUser author
    ) {
        return articleComplaintService.createComplaint(articleId, createDTO, author);
    }

    @PatchMapping("/{id}")
    public ComplaintReadDTO patchComplaint(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody ComplaintPatchDTO patchDTO) {
        return articleComplaintService.patchComplaint(articleId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public ComplaintReadDTO updateComplaint(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody ComplaintPutDTO updateDTO) {
        return articleComplaintService.updateComplaint(articleId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteComplaint(@PathVariable UUID articleId, @PathVariable UUID id) {
        articleComplaintService.deleteComplaint(articleId, id);
    }
}
