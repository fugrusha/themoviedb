package com.golovko.backend.service;

import com.golovko.backend.domain.ActionType;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Like;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.WrongTargetObjectTypeException;
import com.golovko.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TranslationService translationService;

    public LikeReadDTO getLike(UUID userId, UUID id) {
        Like like = getLikeRequired(userId, id);

        return translationService.translate(like, LikeReadDTO.class);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public LikeReadDTO createLike(UUID userId, LikeCreateDTO createDTO) {
        if (likeRepository.findByUserIdAndLikedObjectId(userId, createDTO.getLikedObjectId()) != null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "User's like or dislike for this entity already exists");
        }

        Like like = translationService.translate(createDTO, Like.class);

        like.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        like = likeRepository.save(like);

        if (createDTO.getMeLiked().equals(true)) {
            incrementLikeField(createDTO.getLikedObjectType(), createDTO.getLikedObjectId());
        } else if (createDTO.getMeLiked().equals(false)) {
            incrementDislikeField(createDTO.getLikedObjectType(), createDTO.getLikedObjectId());
        }

        return translationService.translate(like, LikeReadDTO.class);
    }

    private void incrementLikeField(TargetObjectType objectType, UUID likedObjectId) {
        switch (objectType) {
          case MOVIE:
              movieRepository.incrementLikesCountField(likedObjectId);
              break;
          case COMMENT:
              commentRepository.incrementLikesCountField(likedObjectId);
              break;
          case ARTICLE:
              articleRepository.incrementLikesCountField(likedObjectId);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.ADD_LIKE, objectType);
        }
    }

    private void incrementDislikeField(TargetObjectType objectType, UUID likedObjectId) {
        switch (objectType) {
          case MOVIE:
              movieRepository.incrementDislikesCountField(likedObjectId);
              break;
          case COMMENT:
              commentRepository.incrementDislikesCountField(likedObjectId);
              break;
          case ARTICLE:
              articleRepository.incrementDislikesCountField(likedObjectId);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.ADD_DISLIKE, objectType);
        }
    }

    public LikeReadDTO patchLike(UUID userId, UUID id, LikePatchDTO patchDTO) {
        Like like = getLikeRequired(userId, id);

        translationService.map(patchDTO, like);
        like = likeRepository.save(like);

        return translationService.translate(like, LikeReadDTO.class);
    }

    public LikeReadDTO updateLike(UUID userId, UUID id, LikePutDTO updateDTO) {
        Like like = getLikeRequired(userId, id);

        translationService.map(updateDTO, like);
        like = likeRepository.save(like);

        return translationService.translate(like, LikeReadDTO.class);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteLike(UUID userId, UUID id) {
        Like like = getLikeRequired(userId, id);

        if (like.getMeLiked().equals(true)) {
            decrementLikeField(like.getLikedObjectType(), like.getLikedObjectId());
        } else {
            decrementDislikeField(like.getLikedObjectType(), like.getLikedObjectId());
        }

        likeRepository.delete(like);
    }

    private void decrementLikeField(TargetObjectType objectType, UUID likedObjectId) {
        switch (objectType) {
          case MOVIE:
              movieRepository.decrementLikesCountField(likedObjectId);
              break;
          case COMMENT:
              commentRepository.decrementLikesCountField(likedObjectId);
              break;
          case ARTICLE:
              articleRepository.decrementLikesCountField(likedObjectId);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.REMOVE_LIKE, objectType);
        }
    }

    private void decrementDislikeField(TargetObjectType objectType, UUID likedObjectId) {
        switch (objectType) {
          case MOVIE:
              movieRepository.decrementDislikesCountField(likedObjectId);
              break;
          case COMMENT:
              commentRepository.decrementDislikesCountField(likedObjectId);
              break;
          case ARTICLE:
              articleRepository.decrementDislikesCountField(likedObjectId);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.REMOVE_DISLIKE, objectType);
        }
    }

    private Like getLikeRequired(UUID userId, UUID likeId) {
        return Optional.ofNullable(likeRepository.findByIdAndUserId(likeId, userId))
                .orElseThrow(() -> new EntityNotFoundException(Like.class, likeId, userId));
    }
}
