package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArticleCommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TranslationService translationService;

    public CommentReadDTO getComment(UUID articleId, UUID id) {
        Comment comment = getArticleCommentRequired(articleId, id);
        return translationService.toRead(comment);
    }

    public List<CommentReadDTO> getAllComments(UUID articleId) {
        List<Comment> comments = commentRepository.findByParentIdOrderByCreatedAtAsc(articleId, ParentType.ARTICLE);
        return comments.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public List<CommentReadDTO> getAllPublishedComments(UUID articleId) {
        List<Comment> comments =
                commentRepository.findAllByStatusAndParent(articleId, CommentStatus.APPROVED, ParentType.ARTICLE);
        return comments.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public CommentReadDTO createComment(UUID articleId, CommentCreateDTO createDTO, ApplicationUser author) {
        Comment comment = translationService.toEntity(createDTO);

        comment.setStatus(CommentStatus.PENDING);
        comment.setParentId(articleId);
        comment.setAuthor(author);
        comment.setParentType(ParentType.ARTICLE);

        comment = commentRepository.save(comment);
        return translationService.toRead(comment);
    }

    public CommentReadDTO updateComment(UUID articleId, UUID id, CommentPutDTO putDTO) {
        Comment comment = getArticleCommentRequired(articleId, id);

        translationService.updateEntity(putDTO, comment);
        comment = commentRepository.save(comment);

        return translationService.toRead(comment);
    }

    public CommentReadDTO patchComment(UUID articleId, UUID id, CommentPatchDTO patchDTO) {
        Comment comment = getArticleCommentRequired(articleId, id);

        translationService.patchEntity(patchDTO, comment);
        comment = commentRepository.save(comment);

        return translationService.toRead(comment);
    }

    public void deleteComment(UUID articleId, UUID id) {
        commentRepository.delete(getArticleCommentRequired(articleId, id));
    }

    private Comment getArticleCommentRequired(UUID articleId, UUID commentId) {
        Comment comment = commentRepository.findByIdAndParentId(commentId, articleId, ParentType.ARTICLE);

        if (comment != null) {
            return comment;
        } else {
            throw new EntityNotFoundException(Comment.class, commentId, Article.class, articleId);
        }
    }
}
