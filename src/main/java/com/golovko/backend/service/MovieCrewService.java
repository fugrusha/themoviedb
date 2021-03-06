package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.MovieCrewRepository;
import com.golovko.backend.repository.RatingRepository;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class MovieCrewService {

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public PageResult<MovieCrewReadDTO> getAllMovieCrews(UUID movieId, Pageable pageable) {
        Page<MovieCrew> movieCrews = movieCrewRepository.findByMovieId(movieId, pageable);

        return translationService.toPageResult(movieCrews, MovieCrewReadDTO.class);
    }

    public MovieCrewReadDTO getMovieCrew(UUID movieId, UUID id) {
        MovieCrew movieCrew = getMovieCrewByMovieIdRequired(id, movieId);

        return translationService.translate(movieCrew, MovieCrewReadDTO.class);
    }

    @Transactional(readOnly = true)
    public MovieCrewReadExtendedDTO getExtendedMovieCrew(UUID movieId, UUID id) {
        MovieCrew movieCrew = getMovieCrewByMovieIdRequired(id, movieId);

        return translationService.translate(movieCrew, MovieCrewReadExtendedDTO.class);
    }

    public MovieCrewReadDTO createMovieCrew(MovieCrewCreateDTO createDTO, UUID movieId) {
        MovieCrew movieCrew = translationService.translate(createDTO, MovieCrew.class);

        movieCrew.setMovie(repoHelper.getReferenceIfExist(Movie.class, movieId));
        movieCrew = movieCrewRepository.save(movieCrew);

        return translationService.translate(movieCrew, MovieCrewReadDTO.class);
    }

    public MovieCrewReadDTO patchMovieCrew(UUID movieId, UUID id, MovieCrewPatchDTO patchDTO) {
        MovieCrew movieCrew = getMovieCrewByMovieIdRequired(id, movieId);

        translationService.map(patchDTO, movieCrew);
        movieCrew = movieCrewRepository.save(movieCrew);

        return translationService.translate(movieCrew, MovieCrewReadDTO.class);
    }

    public MovieCrewReadDTO updateMovieCrew(UUID movieId, UUID id, MovieCrewPutDTO updateDTO) {
        MovieCrew movieCrew = getMovieCrewByMovieIdRequired(id, movieId);

        translationService.map(updateDTO, movieCrew);
        movieCrew = movieCrewRepository.save(movieCrew);

        return translationService.translate(movieCrew, MovieCrewReadDTO.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMovieCrew(UUID movieId, UUID id) {
        movieCrewRepository.delete(getMovieCrewByMovieIdRequired(id, movieId));
        commentRepository.deleteCommentsByTargetObjectId(id, TargetObjectType.MOVIE_CREW);
        ratingRepository.deleteRatingsByRatedObjectId(id, TargetObjectType.MOVIE_CREW);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAverageRatingOfMovieCrew(UUID movieCrewId) {
        Double averageRating = ratingRepository.calcAverageRating(movieCrewId);
        MovieCrew movieCrew = repoHelper.getEntityById(MovieCrew.class, movieCrewId);

        log.info("Setting new average rating of movieCrew: {}. Old value: {}, new value: {}",
                movieCrew.getId(), movieCrew.getAverageRating(), averageRating);

        movieCrew.setAverageRating(averageRating);
        movieCrewRepository.save(movieCrew);
    }

    private MovieCrew getMovieCrewByMovieIdRequired(UUID id, UUID movieId) {
        return Optional.ofNullable(movieCrewRepository.findByIdAndMovieId(id, movieId))
                .orElseThrow(() -> new EntityNotFoundException(MovieCrew.class, id, movieId));
    }
}
