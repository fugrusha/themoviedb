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

import java.time.LocalDate;
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
    private PersonRepository personRepository;

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

    public PageResult<MoviesTopRatedDTO> getTopRatedMovies(Pageable pageable) {
        Page<MoviesTopRatedDTO> movies = movieRepository.getTopRatedMovies(pageable);
        return translationService.toPageResult(movies);
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateReleasedStatusOfMovie(UUID movieId) {
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);
        LocalDate today = LocalDate.now();

        if (movie.getReleaseDate().isBefore(today) || movie.getReleaseDate().equals(today)) {
            movie.setIsReleased(true);
            movieRepository.save(movie);

            log.info("Release status of movie: {} is set to true", movieId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updatePredictedAverageRatingOfMovie(UUID movieId) {
        Double newPredictedAverageRating = personRepository.calcMovieCastAverageRatingByMovieId(movieId);
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);

        log.info("Setting new predicted average rating of movie: {}. Old value {}, new value {}", movieId,
                movie.getPredictedAverageRating(), newPredictedAverageRating);

        movie.setPredictedAverageRating(newPredictedAverageRating);
        movieRepository.save(movie);
    }
}
