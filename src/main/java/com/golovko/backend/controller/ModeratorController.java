package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.Moderator;
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
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Moderator
public class ModeratorController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ApplicationUserService applicationUserService;

    @ApiOperation(value = "Change trust level of user", notes = "Needs MODERATOR authority")
    @PostMapping("/users/{id}/set-trust-level")
    public UserReadDTO setTrustLevelToUser(
            @PathVariable UUID id,
            @RequestBody @Valid UserTrustLevelDTO dto) {
        return applicationUserService.changeTrustLevel(id, dto);
    }

    @ApiPageable
    @ApiOperation(value = "Get all comments by filter", notes = "Needs MODERATOR authority")
    @GetMapping("/comments")
    public PageResult<CommentReadDTO> getCommentsByFilter(
            CommentFilter filter,
            @ApiIgnore Pageable pageable
    ) {
        return commentService.getCommentsByFilter(filter, pageable);
    }

    @ApiOperation(value = "Moderate comment by id", notes = "Needs MODERATOR authority")
    @PostMapping("/comments/{id}/moderate")
    public CommentReadDTO changeCommentStatus(
            @PathVariable UUID id,
            @RequestBody @Valid CommentModerateDTO dto
    ) {
        return commentService.moderateComment(id, dto);
    }

    @ApiPageable
    @ApiOperation(value = "Get all complaints by filter", notes = "Needs MODERATOR authority")
    @GetMapping("/complaints")
    public PageResult<ComplaintReadDTO> getAllComplaints(
            ComplaintFilter filter,
            @ApiIgnore Pageable pageable
    ) {
        return complaintService.getAllComplaints(filter, pageable);
    }

    @ApiOperation(value = "Moderate complaint", notes = "Needs MODERATOR authority")
    @PostMapping("/complaints/{id}/moderate")
    public ComplaintReadDTO moderateComplaint(
            @PathVariable UUID id,
            @RequestBody @Valid ComplaintModerateDTO dto
    ) {
        return complaintService.moderateComplaint(id, dto);
    }
}
