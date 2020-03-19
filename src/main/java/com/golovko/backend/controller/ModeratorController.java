package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintModerateDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
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
    public ComplaintReadDTO moderateComplaint(
            @PathVariable UUID id,
            @RequestBody ComplaintModerateDTO dto
    ) {
        return complaintService.moderateComplaint(id, dto);
    }
}
