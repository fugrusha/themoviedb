package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.repository.RatingRepository;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieCastService {

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    // TODO pagination
    public List<MovieCastReadDTO> getAllMovieCasts(UUID movieId) {
        List<MovieCast> movieCasts = movieCastRepository.findByMovieId(movieId);

        return movieCasts.stream()
                .map(m -> translationService.translate(m, MovieCastReadDTO.class))
                .collect(Collectors.toList());
    }

    public MovieCastReadDTO getMovieCast(UUID id, UUID movieId) {
        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        return translationService.translate(movieCast, MovieCastReadDTO.class);
    }

    @Transactional(readOnly = true)
    public MovieCastReadExtendedDTO getMovieCastExtended(UUID id, UUID movieId) {
        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        return translationService.translate(movieCast, MovieCastReadExtendedDTO.class);
    }

    public MovieCastReadDTO createMovieCast(MovieCastCreateDTO createDTO, UUID movieId) {
        MovieCast movieCast = translationService.translate(createDTO, MovieCast.class);

        movieCast.setMovie(repoHelper.getReferenceIfExist(Movie.class, movieId));
        movieCast = movieCastRepository.save(movieCast);

        return translationService.translate(movieCast, MovieCastReadDTO.class);
    }

    public MovieCastReadDTO updateMovieCast(MovieCastPutDTO updateDTO, UUID id, UUID movieId) {
        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        translationService.map(updateDTO, movieCast);
        movieCast = movieCastRepository.save(movieCast);

        return translationService.translate(movieCast, MovieCastReadDTO.class);
    }

    public MovieCastReadDTO patchMovieCast(MovieCastPatchDTO patchDTO, UUID id, UUID movieId) {
        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        translationService.map(patchDTO, movieCast);
        movieCast = movieCastRepository.save(movieCast);

        return translationService.translate(movieCast, MovieCastReadDTO.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMovieCast(UUID id, UUID movieId) {
        movieCastRepository.delete(getMovieCastByMovieIdRequired(id, movieId));
        commentRepository.deleteCommentsByTargetObjectId(id, TargetObjectType.MOVIE_CAST);
        ratingRepository.deleteRatingsByRatedObjectId(id, TargetObjectType.MOVIE_CAST);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAverageRatingOfMovieCast(UUID movieCastId) {
        Double averageRating = ratingRepository.calcAverageRating(movieCastId);
        MovieCast movieCast = repoHelper.getEntityById(MovieCast.class, movieCastId);

        log.info("Setting new average rating of movieCast: {}. Old value {}, new value {}", movieCastId,
                movieCast.getAverageRating(), averageRating);

        movieCast.setAverageRating(averageRating);
        movieCastRepository.save(movieCast);
    }

    private MovieCast getMovieCastByMovieIdRequired(UUID id, UUID movieId) {
        return Optional.ofNullable(movieCastRepository.findByIdAndMovieId(id, movieId))
                .orElseThrow(() -> new EntityNotFoundException(MovieCast.class, id, movieId));
    }
}
