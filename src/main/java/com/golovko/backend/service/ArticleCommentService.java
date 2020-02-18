package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
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
        List<Comment> comments = commentRepository.findByParentIdOrderByCreatedAtAsc(articleId);
        return comments.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public List<CommentReadDTO> getAllPublishedComments(UUID articleId) {
        List<Comment> comments =
                commentRepository.findAllByStatusAndByParentId(articleId, CommentStatus.APPROVED);
        return comments.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public CommentReadDTO createComment(UUID articleId, CommentCreateDTO createDTO, ApplicationUser author) {
        Comment comment = translationService.toEntity(createDTO);
        comment.setStatus(CommentStatus.PENDING);
        comment.setParentId(articleId);
        comment.setAuthor(author);
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
        if (commentRepository.findByIdAndParentId(commentId, articleId) != null) {
            return commentRepository.findByIdAndParentId(commentId, articleId);
        } else {
            throw new EntityNotFoundException(Comment.class, commentId, Article.class, articleId);
        }
    }
}
