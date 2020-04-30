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
@RequestMapping("/api/v1/articles/{articleId}/comments")
public class ArticleCommentController {

    @Autowired
    private CommentService commentService;

    @ApiOperation(value = "Get single article comment")
    @GetMapping("/{id}")
    public CommentReadDTO getArticleComment(@PathVariable UUID articleId, @PathVariable UUID id) {
        return commentService.getComment(articleId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all published article comments ")
    @GetMapping
    public PageResult<CommentReadDTO> getPublishedArticleComments(
            @PathVariable UUID articleId,
            @ApiIgnore Pageable pageable
    ) {
        return commentService.getPublishedComments(articleId, pageable);
    }

    @ApiOperation(value = "Create comment for article", notes = "Needs ADMIN authority.")
    @PostMapping
    public CommentReadDTO createArticleComment(
            @PathVariable UUID articleId,
            @RequestBody @Valid CommentCreateDTO createDTO
    ) {
        return commentService.createComment(articleId, createDTO);
    }

    @ApiOperation(value = "Update article comment")
    @PutMapping("/{id}")
    public CommentReadDTO updateArticleComment(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPutDTO putDTO
    ) {
        return commentService.updateComment(articleId, id, putDTO);
    }

    @ApiOperation(value = "Update article comment")
    @PatchMapping("/{id}")
    public CommentReadDTO patchArticleComment(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(articleId, id, patchDTO);
    }

    @ApiOperation(value = "Delete article comment")
    @DeleteMapping("/{id}")
    public void deleteArticleComment(@PathVariable UUID articleId, @PathVariable UUID id) {
        commentService.deleteComment(articleId, id);
    }
}
