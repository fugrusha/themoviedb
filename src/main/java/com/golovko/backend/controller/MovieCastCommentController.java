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
@RequestMapping("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments")
public class MovieCastCommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}")
    public CommentReadDTO getMovieCastComment(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id) {
        return commentService.getComment(movieCastId, id);
    }

    @GetMapping
    public List<CommentReadDTO> getAllPublishedMovieCastComments(@PathVariable UUID movieCastId) {
        return commentService.getAllPublishedComments(movieCastId);
    }

    @PostMapping
    public CommentReadDTO createMovieCastComment(
            @PathVariable UUID movieCastId,
            @RequestBody CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieCastId, createDTO);
    }

    @PutMapping("/{id}")
    public CommentReadDTO updateMovieCastComment(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id,
            @RequestBody CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieCastId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieCastComment(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id,
            @RequestBody CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieCastId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCastComment(@PathVariable UUID movieCastId, @PathVariable UUID id) {
        commentService.deleteComment(movieCastId, id);
    }
}
