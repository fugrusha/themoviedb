package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentReadDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/moderator")
public class ModeratorController {

    @GetMapping("/comments")
    public List<CommentReadDTO> getAllComments() {
        return null;
    }

}
