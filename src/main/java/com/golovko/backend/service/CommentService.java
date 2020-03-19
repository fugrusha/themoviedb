package com.golovko.backend.service;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.golovko.backend.domain.TargetObjectType.COMMENT;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TranslationService translationService;

    public CommentReadDTO getComment(UUID targetObjectId, UUID id) {
        Comment comment = getCommentRequired(targetObjectId, id);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    public List<CommentReadDTO> getAllComments(UUID targetObjectId) {
        List<Comment> comments = commentRepository.findAllByTargetIdOrderByCreatedAtAsc(targetObjectId);

        return comments.stream()
                .map(c -> translationService.translate(c, CommentReadDTO.class))
                .collect(Collectors.toList());
    }

    public List<CommentReadDTO> getAllPublishedComments(UUID targetObjectId) {
        List<Comment> comments = commentRepository.findAllByStatusAndTarget(targetObjectId, CommentStatus.APPROVED);

        return comments.stream()
                .map(c -> translationService.translate(c, CommentReadDTO.class))
                .collect(Collectors.toList());
    }

    public CommentReadDTO createComment(UUID targetObjectId, CommentCreateDTO createDTO) {
        Comment comment = translationService.translate(createDTO, Comment.class);

        comment.setStatus(CommentStatus.PENDING);
        comment.setTargetObjectId(targetObjectId);
        comment = commentRepository.save(comment);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    public CommentReadDTO updateComment(UUID targetObjectId, UUID id, CommentPutDTO putDTO) {
        Comment comment = getCommentRequired(targetObjectId, id);

        translationService.map(putDTO, comment);
        comment = commentRepository.save(comment);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    public CommentReadDTO patchComment(UUID targetObjectId, UUID id, CommentPatchDTO patchDTO) {
        Comment comment = getCommentRequired(targetObjectId, id);

        translationService.map(patchDTO, comment);
        comment = commentRepository.save(comment);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteComment(UUID targetObjectId, UUID id) {
        commentRepository.delete(getCommentRequired(targetObjectId, id));
        likeRepository.deleteLikesByTargetObjectId(id, COMMENT);
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
