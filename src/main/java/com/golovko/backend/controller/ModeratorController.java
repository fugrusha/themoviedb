package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.moderator.ModeratorDTO;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderator")
public class ModeratorController {

    // TODO take complaint for moderation
    // TODO change status of complaint
    // TODO set trustLevel for users

    @Autowired
    private ComplaintService complaintService;

    // TODO
    @GetMapping("/comments")
    public List<CommentReadDTO> getAllComments() {
        return null;
    }

    @GetMapping("/complaints")
    public List<ComplaintReadDTO> getAllComplaints(ComplaintFilter filter) {
        return complaintService.getAllComplaints(filter);
    }

    @PostMapping("/complaints/{id}/moderate")
    public ComplaintReadDTO takeForModeration(@RequestParam UUID id, @RequestBody ModeratorDTO dto) {
        return complaintService.takeForModeration(id, dto);
    }

    @PostMapping("/complaints/{id}/change-status")
    public ComplaintReadDTO changeComplaintStatus(@RequestParam UUID id, @RequestBody ModeratorDTO dto) {
        return complaintService.changeStatus(id, dto);
    }
}
