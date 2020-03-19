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
@RequestMapping("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments")
public class MovieCrewCommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}")
    public CommentReadDTO getMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id) {
        return commentService.getComment(movieCrewId, id);
    }

    @GetMapping
    public List<CommentReadDTO> getAllPublishedMovieCrewComments(@PathVariable UUID movieCrewId) {
        return commentService.getAllPublishedComments(movieCrewId);
    }

    @PostMapping
    public CommentReadDTO createMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @RequestBody CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieCrewId, createDTO);
    }

    @PutMapping("/{id}")
    public CommentReadDTO updateMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieCrewId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieCrewId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCrewComment(@PathVariable UUID movieCrewId, @PathVariable UUID id) {
        commentService.deleteComment(movieCrewId, id);
    }
}
