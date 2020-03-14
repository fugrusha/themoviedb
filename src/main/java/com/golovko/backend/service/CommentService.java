package com.golovko.backend.service;

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
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TranslationService translationService;

    public CommentReadDTO getComment(UUID targetObjectId, UUID id) {
        Comment comment = getCommentRequired(targetObjectId, id);
        return translationService.toRead(comment);
    }

    public List<CommentReadDTO> getAllComments(UUID targetObjectId) {
        List<Comment> comments = commentRepository.findAllByTargetIdOrderByCreatedAtAsc(targetObjectId);
        return comments.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public List<CommentReadDTO> getAllPublishedComments(UUID targetObjectId) {
        List<Comment> comments = commentRepository.findAllByStatusAndTarget(targetObjectId, CommentStatus.APPROVED);
        return comments.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public CommentReadDTO createComment(UUID targetObjectId, CommentCreateDTO createDTO) {
        Comment comment = translationService.toEntity(createDTO);

        comment.setStatus(CommentStatus.PENDING);
        comment.setTargetObjectId(targetObjectId);

        comment = commentRepository.save(comment);
        return translationService.toRead(comment);
    }

    public CommentReadDTO updateComment(UUID targetObjectId, UUID id, CommentPutDTO putDTO) {
        Comment comment = getCommentRequired(targetObjectId, id);

        translationService.updateEntity(putDTO, comment);
        comment = commentRepository.save(comment);

        return translationService.toRead(comment);
    }

    public CommentReadDTO patchComment(UUID targetObjectId, UUID id, CommentPatchDTO patchDTO) {
        Comment comment = getCommentRequired(targetObjectId, id);

        translationService.patchEntity(patchDTO, comment);
        comment = commentRepository.save(comment);

        return translationService.toRead(comment);
    }

    public void deleteComment(UUID targetObjectId, UUID id) {
        commentRepository.delete(getCommentRequired(targetObjectId, id));
    }

    private Comment getCommentRequired(UUID targetObjectId, UUID commentId) {
        Comment comment = commentRepository.findByIdAndTargetId(commentId, targetObjectId);

        if (comment != null) {
            return comment;
        } else {
            throw new EntityNotFoundException(Comment.class, commentId, targetObjectId);
        }
    }
}
