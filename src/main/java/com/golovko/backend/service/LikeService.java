package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Like;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TranslationService translationService;

    public LikeReadDTO getLike(UUID userId, UUID id) {
        Like like = getLikeRequired(userId, id);

        return translationService.translate(like, LikeReadDTO.class);
    }

    public LikeReadDTO createLike(UUID userId, LikeCreateDTO createDTO) {
        Like like = translationService.translate(createDTO, Like.class);

        like.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        like = likeRepository.save(like);

        return translationService.translate(like, LikeReadDTO.class);
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

    public void deleteLike(UUID userId, UUID id) {
        likeRepository.delete(getLikeRequired(userId, id));
    }

    private Like getLikeRequired(UUID userId, UUID likeId) {
        Like like = likeRepository.findByIdAndUserId(likeId, userId);

        if (like != null) {
            return like;
        } else {
            throw new EntityNotFoundException(Like.class, likeId, userId);
        }
    }
}
