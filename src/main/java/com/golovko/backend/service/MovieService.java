package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.RatingRepository;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public MovieReadDTO getMovie(UUID id) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);
        return translationService.toRead(movie);
    }

    public List<MovieReadDTO> getMovies(MovieFilter filter) {
        List<Movie> movies = movieRepository.findByFilter(filter);
        return movies.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MovieReadExtendedDTO getMovieExtended(UUID id) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);
        return translationService.toReadExtended(movie);
    }

    public MovieReadDTO createMovie(MovieCreateDTO createDTO) {
        Movie movie = translationService.toEntity(createDTO);

        movie = movieRepository.save(movie);

        return translationService.toRead(movie);
    }

    public MovieReadDTO patchMovie(UUID id, MoviePatchDTO patchDTO) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);

        translationService.patchEntity(patchDTO, movie);

        movie = movieRepository.save(movie);

        return translationService.toRead(movie);
    }

    public MovieReadDTO updateMovie(UUID id, MoviePutDTO updateDTO) {
        Movie movie = repoHelper.getEntityById(Movie.class, id);

        translationService.updateEntity(updateDTO, movie);

        movie = movieRepository.save(movie);

        return translationService.toRead(movie);
    }

    public void deleteMovie(UUID id) {
        movieRepository.delete(repoHelper.getEntityById(Movie.class, id));
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
