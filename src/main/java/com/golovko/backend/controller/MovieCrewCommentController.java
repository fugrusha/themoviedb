package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public PageResult<CommentReadDTO> getPublishedMovieCrewComments(
            @PathVariable UUID movieCrewId,
            Pageable pageable
    ) {
        return commentService.getPublishedComments(movieCrewId, pageable);
    }

    @PostMapping
    public CommentReadDTO createMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @RequestBody @Valid CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieCrewId, createDTO);
    }

    @PutMapping("/{id}")
    public CommentReadDTO updateMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieCrewId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieCrewId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCrewComment(@PathVariable UUID movieCrewId, @PathVariable UUID id) {
        commentService.deleteComment(movieCrewId, id);
    }
}
