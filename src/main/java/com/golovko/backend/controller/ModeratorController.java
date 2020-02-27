package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moderator")
public class ModeratorController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/comments")
    public List<CommentReadDTO> getAllComments() {
        return null;
    }

}
