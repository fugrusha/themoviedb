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
        return translationService.toRead(like);
    }

    public LikeReadDTO createLike(UUID userId, LikeCreateDTO createDTO) {
        Like like = translationService.toEntity(createDTO);

        like.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        like = likeRepository.save(like);
        return translationService.toRead(like);
    }

    public LikeReadDTO patchLike(UUID userId, UUID id, LikePatchDTO patchDTO) {
        Like like = getLikeRequired(userId, id);

        translationService.patchEntity(patchDTO, like);

        like = likeRepository.save(like);
        return translationService.toRead(like);
    }

    public LikeReadDTO updateLike(UUID userId, UUID id, LikePutDTO updateDTO) {
        Like like = getLikeRequired(userId, id);

        translationService.updateEntity(updateDTO, like);

        like = likeRepository.save(like);
        return translationService.toRead(like);
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
