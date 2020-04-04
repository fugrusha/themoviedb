package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.CommentFilter;
import com.golovko.backend.dto.comment.CommentModerateDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintModerateDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.dto.user.UserTrustLevelDTO;
import com.golovko.backend.service.ApplicationUserService;
import com.golovko.backend.service.CommentService;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public PageResult<CommentReadDTO> getCommentsByFilter(CommentFilter filter, Pageable pageable) {
        return commentService.getCommentsByFilter(filter, pageable);
    }

    @PostMapping("/comments/{id}/moderate")
    public CommentReadDTO changeCommentStatus(
            @PathVariable UUID id,
            @RequestBody @Valid CommentModerateDTO dto
    ) {
        return commentService.moderateComment(id, dto);
    }

    @GetMapping("/complaints")
    public PageResult<ComplaintReadDTO> getAllComplaints(ComplaintFilter filter, Pageable pageable) {
        return complaintService.getAllComplaints(filter, pageable);
    }

    @PostMapping("/complaints/{id}/moderate")
    public ComplaintReadDTO moderateComplaint(
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintModerateDTO dto
    ) {
        return complaintService.moderateComplaint(id, dto);
    }
}
