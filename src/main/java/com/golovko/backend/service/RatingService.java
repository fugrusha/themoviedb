package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.exception.WrongTargetObjectTypeException;
import com.golovko.backend.repository.RatingRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TranslationService translationService;

    public PageResult<RatingReadDTO> getRatingsByTargetObjectId(UUID targetId, Pageable pageable) {
        Page<Rating> ratings = ratingRepository.findAllByTargetId(targetId, pageable);

        return translationService.toPageResult(ratings, RatingReadDTO.class);
    }

    public RatingReadDTO getRating(UUID targetId, UUID id) {
        Rating rating = getRatingRequired(targetId, id);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    @Transactional
    public RatingReadDTO createRating(UUID targetId, RatingCreateDTO createDTO) {
        Rating rating = translationService.translate(createDTO, Rating.class);

        validateTargetObject(targetId, createDTO);

        rating.setRatedObjectId(targetId);
        rating = ratingRepository.save(rating);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    private void validateTargetObject(UUID targetId, RatingCreateDTO createDTO) {
        switch (createDTO.getRatedObjectType()) {
          case MOVIE_CAST:
              MovieCast movieCast = repoHelper.getReferenceIfExist(MovieCast.class, targetId);
              checkIfMovieIsReleased(movieCast.getMovie());
              break;
          case MOVIE_CREW:
              MovieCrew movieCrew = repoHelper.getReferenceIfExist(MovieCrew.class, targetId);
              checkIfMovieIsReleased(movieCrew.getMovie());
              break;
          case MOVIE:
              Movie movie = repoHelper.getReferenceIfExist(Movie.class, targetId);
              checkIfMovieIsReleased(movie);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.ADD_RATING, createDTO.getRatedObjectType());
        }
    }

    private void checkIfMovieIsReleased(Movie movie) {
        if (!movie.getIsReleased()) {
            throw new EntityWrongStatusException("It's not allowed to rate unreleased movies.");
        }
    }

    public RatingReadDTO patchRating(UUID targetId, UUID id, RatingPatchDTO patchDTO) {
        Rating rating = getRatingRequired(targetId, id);

        translationService.map(patchDTO, rating);
        rating = ratingRepository.save(rating);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    public RatingReadDTO updateRating(UUID targetId, UUID id, RatingPutDTO putDTO) {
        Rating rating = getRatingRequired(targetId, id);

        translationService.map(putDTO, rating);
        rating = ratingRepository.save(rating);

        return translationService.translate(rating, RatingReadDTO.class);
    }

    public void deleteRating(UUID targetId, UUID id) {
        ratingRepository.delete(getRatingRequired(targetId, id));
    }

    private Rating getRatingRequired(UUID targetId, UUID ratingId) {
        return Optional.ofNullable(ratingRepository.findByIdAndTargetId(ratingId, targetId))
                .orElseThrow(() -> new EntityNotFoundException(Rating.class, ratingId, targetId));
    }
}
