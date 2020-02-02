package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    TranslationService translationService;

    public MovieReadDTO getMovie(UUID id) {
        Movie movie = getMovieRequired(id);
        return translationService.toRead(movie);
    }

    public List<MovieReadDTO> getMovies(MovieFilter filter) {
        List<Movie> movies = movieRepository.findByFilter(filter);
        return movies.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public MovieReadDTO createMovie(MovieCreateDTO createDTO) {
        Movie movie = translationService.toEntity(createDTO);

        movie = movieRepository.save(movie);

        return translationService.toRead(movie);
    }

    public MovieReadDTO patchMovie(UUID id, MoviePatchDTO patchDTO) {
        Movie movie = getMovieRequired(id);

        translationService.patchEntity(patchDTO, movie);

        movie = movieRepository.save(movie);

        return translationService.toRead(movie);
    }

    public MovieReadDTO updateMovie(UUID id, MoviePutDTO updateDTO) {
        Movie movie = getMovieRequired(id);

        translationService.updateEntity(updateDTO, movie);

        movie = movieRepository.save(movie);

        return translationService.toRead(movie);
    }

    public void deleteMovie(UUID id) {
        movieRepository.delete(getMovieRequired(id));
    }

    private Movie getMovieRequired(UUID id) {
        return movieRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Movie.class, id)
        );
    }
}
