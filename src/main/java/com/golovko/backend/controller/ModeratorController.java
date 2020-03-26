package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentFilter;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.comment.CommentStatusDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintModerateDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.dto.user.UserTrustLevelDTO;
import com.golovko.backend.service.ApplicationUserService;
import com.golovko.backend.service.CommentService;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ModeratorController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ApplicationUserService applicationUserService;

    @PostMapping("/users/{id}/set-trust-level")
    public UserReadDTO setTrustLevelToUser(
            @PathVariable UUID id,
            @RequestBody @Valid UserTrustLevelDTO dto) {
        return applicationUserService.changeTrustLevel(id, dto);
    }

    @GetMapping("/comments")
    public List<CommentReadDTO> getCommentsByFilter(CommentFilter filter) {
        return commentService.getCommentsByFilter(filter);
    }

    @PostMapping("/comments/{id}/change-status")
    public CommentReadDTO changeCommentStatus(
            @PathVariable UUID id,
            @RequestBody @Valid CommentStatusDTO dto
    ) {
        return commentService.changeStatus(id, dto);
    }

    @GetMapping("/complaints")
    public List<ComplaintReadDTO> getAllComplaints(ComplaintFilter filter) {
        return complaintService.getAllComplaints(filter);
    }

    @PostMapping("/complaints/{id}/moderate")
    public ComplaintReadDTO moderateComplaint(
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintModerateDTO dto
    ) {
        return complaintService.moderateComplaint(id, dto);
    }
}
