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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TranslationService translationService;

    // TODO pagination
    public List<RatingReadDTO> getAllRatingsByTargetObjectId(UUID targetId) {
        List<Rating> ratings = ratingRepository.findAllByTargetId(targetId);

        return ratings.stream()
                .map(r -> translationService.translate(r, RatingReadDTO.class))
                .collect(Collectors.toList());
    }

    public RatingReadDTO getRating(UUID movieId, UUID id) {
        Rating rating = getRatingRequired(movieId, id);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    public RatingReadDTO createRating(UUID movieId, RatingCreateDTO createDTO) {
        Rating rating = translationService.translate(createDTO, Rating.class);

        rating.setRatedObjectId(movieId);
        rating = ratingRepository.save(rating);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    public RatingReadDTO patchRating(UUID movieId, UUID id, RatingPatchDTO patchDTO) {
        Rating rating = getRatingRequired(movieId, id);

        translationService.map(patchDTO, rating);
        rating = ratingRepository.save(rating);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    public RatingReadDTO updateRating(UUID movieId, UUID id, RatingPutDTO putDTO) {
        Rating rating = getRatingRequired(movieId, id);

        translationService.map(putDTO, rating);
        rating = ratingRepository.save(rating);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    public void deleteRating(UUID movieId, UUID id) {
        ratingRepository.delete(getRatingRequired(movieId, id));
    }

    private Rating getRatingRequired(UUID targetId, UUID ratingId) {
        return Optional.ofNullable(ratingRepository.findByIdAndTargetId(ratingId, targetId))
                .orElseThrow(() -> new EntityNotFoundException(Rating.class, ratingId, targetId));
    }
}
