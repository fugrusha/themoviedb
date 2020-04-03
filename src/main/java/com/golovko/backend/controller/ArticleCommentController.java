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
@RequestMapping("/api/v1/articles/{articleId}/comments")
public class ArticleCommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{id}")
    public CommentReadDTO getArticleComment(@PathVariable UUID articleId, @PathVariable UUID id) {
        return commentService.getComment(articleId, id);
    }

    @GetMapping
    public PageResult<CommentReadDTO> getPublishedArticleComments(
            @PathVariable UUID articleId,
            Pageable pageable
    ) {
        return commentService.getPublishedComments(articleId, pageable);
    }

    @PostMapping
    public CommentReadDTO createArticleComment(
            @PathVariable UUID articleId,
            @RequestBody @Valid CommentCreateDTO createDTO
    ) {
        return commentService.createComment(articleId, createDTO);
    }

    @PutMapping("/{id}")
    public CommentReadDTO updateArticleComment(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPutDTO putDTO
    ) {
        return commentService.updateComment(articleId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public CommentReadDTO patchArticleComment(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody @Valid CommentPatchDTO patchDTO
    ) {
        return commentService.patchComment(articleId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteArticleComment(@PathVariable UUID articleId, @PathVariable UUID id) {
        commentService.deleteComment(articleId, id);
    }
}
