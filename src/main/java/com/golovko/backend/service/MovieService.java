package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MoviePatchDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    public void deleteMovie(UUID id) {
        movieRepository.delete(getMovieRequired(id));
    }

    private Movie getMovieRequired(UUID id) {
        return movieRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Movie.class, id)
        );
    }
}
