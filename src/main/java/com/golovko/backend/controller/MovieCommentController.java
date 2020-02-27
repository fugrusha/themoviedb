package com.golovko.backend.controller;

import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/comments")
public class MovieCommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}")
    public CommentReadDTO getMovieComment(@PathVariable UUID movieId, @PathVariable UUID id) {
        return commentService.getComment(movieId, id);
    }

    // for users
    @GetMapping
    public List<CommentReadDTO> getAllPublishedMovieComments(@PathVariable UUID movieId) {
        return commentService.getAllPublishedComments(movieId);
    }

    // for moderator
    @GetMapping("/moderator")
    public List<CommentReadDTO> getAllMovieComments(@PathVariable UUID movieId) {
        return commentService.getAllComments(movieId);
    }

    @PostMapping
    public CommentReadDTO createMovieComment(
            @PathVariable UUID movieId,
            @RequestBody CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieId, createDTO);
    }

    @PutMapping("/{id}")
    public CommentReadDTO updateMovieComment(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieComment(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieComment(@PathVariable UUID movieId, @PathVariable UUID id) {
        commentService.deleteComment(movieId, id);
    }
}