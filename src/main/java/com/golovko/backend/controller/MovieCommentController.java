package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.service.CommentService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/comments")
public class MovieCommentController {

    @Autowired
    private CommentService commentService;

    @ApiOperation(value = "Get single comment for movie")
    @GetMapping("/{id}")
    public CommentReadDTO getMovieComment(@PathVariable UUID movieId, @PathVariable UUID id) {
        return commentService.getComment(movieId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all published comments for movie")
    @GetMapping
    public PageResult<CommentReadDTO> getPublishedMovieComments(
            @PathVariable UUID movieId,
            @ApiIgnore Pageable pageable
    ) {
        return commentService.getPublishedComments(movieId, pageable);
    }

    @ApiOperation(value = "Create comment for movie")
    @PostMapping
    public CommentReadDTO createMovieComment(
            @PathVariable UUID movieId,
            @RequestBody @Valid CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieId, createDTO);
    }

    @ApiOperation(value = "Update comment for movie")
    @PutMapping("/{id}")
    public CommentReadDTO updateMovieComment(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieId, id, putDTO);
    }

    @ApiOperation(value = "Update comment for movie")
    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieComment(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieId, id, patchDTO);
    }

    @ApiOperation(value = "Delete comment for movie")
    @DeleteMapping("/{id}")
    public void deleteMovieComment(@PathVariable UUID movieId, @PathVariable UUID id) {
        commentService.deleteComment(movieId, id);
    }
}
