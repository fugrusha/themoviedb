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
@RequestMapping("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/comments")
public class MovieCrewCommentController {

    @Autowired
    private CommentService commentService;

    @ApiOperation(value = "Get single comment for movie crew")
    @GetMapping("/{id}")
    public CommentReadDTO getMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id
    ) {
        return commentService.getComment(movieCrewId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all comments for movie crew")
    @GetMapping
    public PageResult<CommentReadDTO> getPublishedMovieCrewComments(
            @PathVariable UUID movieCrewId,
            @ApiIgnore Pageable pageable
    ) {
        return commentService.getPublishedComments(movieCrewId, pageable);
    }

    @ApiOperation(value = "Create comment for movie crew")
    @PostMapping
    public CommentReadDTO createMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @RequestBody @Valid CommentCreateDTO createDTO
    ) {
        return commentService.createComment(movieCrewId, createDTO);
    }

    @ApiOperation(value = "Update comment for movie crew")
    @PutMapping("/{id}")
    public CommentReadDTO updateMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPutDTO putDTO
    ) {
        return commentService.updateComment(movieCrewId, id, putDTO);
    }

    @ApiOperation(value = "Update comment for movie crew")
    @PatchMapping("/{id}")
    public CommentReadDTO patchMovieCrewComment(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(movieCrewId, id, patchDTO);
    }

    @ApiOperation(value = "Delete comment for movie crew")
    @DeleteMapping("/{id}")
    public void deleteMovieCrewComment(@PathVariable UUID movieCrewId, @PathVariable UUID id) {
        commentService.deleteComment(movieCrewId, id);
    }
}
