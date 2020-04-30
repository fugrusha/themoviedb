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
@RequestMapping("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/comments")
public class MovieCastCommentController {

    @Autowired
    private CommentService commentService;

    @ApiOperation(value = "Get single movie cast comment")
    @GetMapping("/{id}")
    public CommentReadDTO getMovieCastComment(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id) {
        return commentService.getComment(movieCastId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all published movie cast comments")
    @GetMapping
    public PageResult<CommentReadDTO> getAllPublishedMovieCastComments(
            @PathVariable UUID movieCastId,
            @ApiIgnore Pageable pageable
    ) {
        return commentService.getPublishedComments(movieCastId, pageable);
    }

    @ApiOperation(value = "Create comment for movie cast")
    @PostMapping
    public CommentReadDTO createMovieCastComment(
            @PathVariable UUID movieCastId,
            @RequestBody @Valid CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieCastId, createDTO);
    }

    @ApiOperation(value = "Update movie cast comment")
    @PutMapping("/{id}")
    public CommentReadDTO updateMovieCastComment(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieCastId, id, putDTO);
    }

    @ApiOperation(value = "Update movie cast comment")
    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieCastComment(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieCastId, id, patchDTO);
    }

    @ApiOperation(value = "Delete movie cast comment")
    @DeleteMapping("/{id}")
    public void deleteMovieCastComment(@PathVariable UUID movieCastId, @PathVariable UUID id) {
        commentService.deleteComment(movieCastId, id);
    }
}
