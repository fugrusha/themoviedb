package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.*;
import com.golovko.backend.exception.BlockedUserException;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.WrongTargetObjectTypeException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.COMMENT;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public CommentReadDTO getComment(UUID targetObjectId, UUID id) {
        Comment comment = getCommentRequired(targetObjectId, id);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    public PageResult<CommentReadDTO> getCommentsByFilter(CommentFilter filter, Pageable pageable) {
        Page<Comment> comments = commentRepository.findByFilter(filter, pageable);

        return translationService.toPageResult(comments, CommentReadDTO.class);
    }

    public PageResult<CommentReadDTO> getPublishedComments(UUID targetObjectId, Pageable pageable) {
        Page<Comment> comments = commentRepository
                .findAllByStatusAndTarget(targetObjectId, CommentStatus.APPROVED, pageable);

        return translationService.toPageResult(comments, CommentReadDTO.class);
    }

    @Transactional
    public CommentReadDTO createComment(UUID targetObjectId, CommentCreateDTO createDTO) {
        ApplicationUser user = repoHelper.getReferenceIfExist(ApplicationUser.class, createDTO.getAuthorId());

        if (user.getIsBlocked()) {
            throw new BlockedUserException(user.getId());
        }

        validateTargetObject(targetObjectId, createDTO);

        Comment comment = translationService.translate(createDTO, Comment.class);

        if (user.getTrustLevel() < 5) {
            comment.setStatus(CommentStatus.PENDING);
        } else {
            comment.setStatus(CommentStatus.APPROVED);
        }

        comment.setAuthor(user);
        comment.setTargetObjectId(targetObjectId);
        comment = commentRepository.save(comment);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    private void validateTargetObject(UUID targetObjectId, CommentCreateDTO createDTO) {
        switch (createDTO.getTargetObjectType()) {
          case MOVIE_CAST:
              repoHelper.getReferenceIfExist(MovieCast.class, targetObjectId);
              break;
          case MOVIE_CREW:
              repoHelper.getReferenceIfExist(MovieCrew.class, targetObjectId);
              break;
          case MOVIE:
              repoHelper.getReferenceIfExist(Movie.class, targetObjectId);
              break;
          case ARTICLE:
              repoHelper.getReferenceIfExist(Article.class, targetObjectId);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.ADD_COMMENT, createDTO.getTargetObjectType());
        }
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

    public CommentReadDTO moderateComment(UUID id, CommentModerateDTO dto) {
        Comment comment = repoHelper.getEntityById(Comment.class, id);

        if (dto.getNewMessage() != null) {
            comment.setMessage(dto.getNewMessage());
        }

        comment.setStatus(dto.getNewStatus());
        comment = commentRepository.save(comment);

        return translationService.translate(comment, CommentReadDTO.class);
    }

    private Comment getCommentRequired(UUID targetObjectId, UUID commentId) {
        return Optional.ofNullable(commentRepository.findByIdAndTargetId(commentId, targetObjectId))
                .orElseThrow(() -> new EntityNotFoundException(Comment.class, commentId, targetObjectId));
    }
}
