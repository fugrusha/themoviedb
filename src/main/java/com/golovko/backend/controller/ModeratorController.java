package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moderator")
public class ModeratorController {

    // TODO take complaint for moderation
    // TODO list my moderated complaints
    // TODO change status of complaint
    // TODO set trustLevel for users

    @Autowired
    private ComplaintService complaintService;

    @GetMapping("/comments")
    public List<CommentReadDTO> getAllComments() {
        return null;
    }

    @GetMapping("/complaints")
    public List<ComplaintReadDTO> getAllComplaints(ComplaintFilter filter) {
        return complaintService.getAllComplaints(filter);
    }

}
