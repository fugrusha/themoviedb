package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

@Slf4j
@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public MovieReadDTO getMovie(UUID id) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);

        return translationService.translate(movie, MovieReadDTO.class);
    }

    public PageResult<MovieReadDTO> getMovies(MovieFilter filter, Pageable pageable) {
        Page<Movie> movies = movieRepository.findByFilter(filter, pageable);

        return translationService.toPageResult(movies, MovieReadDTO.class);
    }

    @Transactional(readOnly = true)
    public MovieReadExtendedDTO getMovieExtended(UUID id) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);

        return translationService.translate(movie, MovieReadExtendedDTO.class);
    }

    public MovieReadDTO createMovie(MovieCreateDTO createDTO) {
        Movie movie = translationService.translate(createDTO, Movie.class);

        movie = movieRepository.save(movie);

        return translationService.translate(movie, MovieReadDTO.class);
    }

    public MovieReadDTO patchMovie(UUID id, MoviePatchDTO patchDTO) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);

        translationService.map(patchDTO, movie);
        movie = movieRepository.save(movie);

        return translationService.translate(movie, MovieReadDTO.class);
    }

    public MovieReadDTO updateMovie(UUID id, MoviePutDTO updateDTO) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);

        translationService.map(updateDTO, movie);
        movie = movieRepository.save(movie);

        return translationService.translate(movie, MovieReadDTO.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteMovie(UUID id) {
        movieRepository.delete(repoHelper.getEntityById(Movie.class, id));
        commentRepository.deleteCommentsByTargetObjectId(id, MOVIE);
        likeRepository.deleteLikesByTargetObjectId(id, MOVIE);
        ratingRepository.deleteRatingsByRatedObjectId(id, MOVIE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateAverageRatingOfMovie(UUID movieId) {
        Double averageRating = ratingRepository.calcAverageRating(movieId);
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);

        log.info("Setting new average rating of movie: {}. Old value {}, new value {}", movieId,
                movie.getAverageRating(), averageRating);

        movie.setAverageRating(averageRating);
        movieRepository.save(movie);
    }
}
