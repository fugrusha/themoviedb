package com.golovko.backend.controller;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.service.ArticleCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/articles/{articleId}/comments")
public class ArticleCommentController {

    @Autowired
    private ArticleCommentService articleCommentService;

    @GetMapping("/{id}")
    public CommentReadDTO getArticleComment(@PathVariable UUID articleId, @PathVariable UUID id) {
        return articleCommentService.getComment(articleId, id);
    }

    // for users
    @GetMapping
    public List<CommentReadDTO> getAllPublishedArticleComments(@PathVariable UUID articleId) {
        return articleCommentService.getAllPublishedComments(articleId);
    }

    // for moderator
    @GetMapping("/moderator")
    public List<CommentReadDTO> getAllArticleComments(@PathVariable UUID articleId) {
        return articleCommentService.getAllComments(articleId);
    }

    @PostMapping
    public CommentReadDTO createArticleComment(
            @PathVariable UUID articleId,
            CommentCreateDTO createDTO,
            ApplicationUser author) {
        return articleCommentService.createComment(articleId, createDTO, author);
    }

    @PutMapping("/{id}")
    public CommentReadDTO updateArticleComment(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody CommentPutDTO putDTO
    ) {
        return articleCommentService.updateComment(articleId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public CommentReadDTO patchArticleComment(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody CommentPatchDTO patchDTO
    ) {
        return articleCommentService.patchComment(articleId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteArticleComment(@PathVariable UUID articleId, @PathVariable UUID id) {
        articleCommentService.deleteComment(articleId, id);
    }
}
