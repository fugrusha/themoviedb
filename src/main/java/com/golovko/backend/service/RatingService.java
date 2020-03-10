package com.golovko.backend.service;

import com.golovko.backend.domain.Rating;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TranslationService translationService;

    public List<RatingReadDTO> getAllRatingsByTargetObjectId(UUID targetId) {
        List<Rating> ratings = ratingRepository.findAllByTargetId(targetId);
        return ratings.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public RatingReadDTO getRating(UUID movieId, UUID id) {
        Rating rating = getRatingRequired(movieId, id);
        return translationService.toRead(rating);
    }

    public RatingReadDTO createRating(UUID movieId, RatingCreateDTO createDTO) {
        Rating rating = translationService.toEntity(createDTO);
        rating.setRatedObjectId(movieId);

        rating = ratingRepository.save(rating);
        return translationService.toRead(rating);
    }

    public RatingReadDTO patchRating(UUID movieId, UUID id, RatingPatchDTO patchDTO) {
        Rating rating = getRatingRequired(movieId, id);

        translationService.patchEntity(patchDTO, rating);

        rating = ratingRepository.save(rating);
        return translationService.toRead(rating);
    }

    public RatingReadDTO updateRating(UUID movieId, UUID id, RatingPutDTO putDTO) {
        Rating rating = getRatingRequired(movieId, id);

        translationService.updateEntity(putDTO, rating);

        rating = ratingRepository.save(rating);
        return translationService.toRead(rating);
    }

    public void deleteRating(UUID movieId, UUID id) {
        ratingRepository.delete(getRatingRequired(movieId, id));
    }

    private Rating getRatingRequired(UUID targetId, UUID ratingId) {
        Rating rating = ratingRepository.findByIdAndTargetId(ratingId, targetId);

        if (rating != null) {
            return rating;
        } else {
            throw new EntityNotFoundException(Rating.class, ratingId, targetId);
        }
    }
}
