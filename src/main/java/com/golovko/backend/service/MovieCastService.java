package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MovieCastService {

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TranslationService translationService;

    public MovieCastReadDTO getMovieCast(UUID id) {
        return translationService.toRead(getMovieCastRequired(id));
    }

    public MovieCastReadExtendedDTO getMovieCastExtended(UUID id) {
        return translationService.toReadExtended(getMovieCastRequired(id));
    }

    public MovieCastReadDTO createMovieCast(MovieCastCreateDTO createDTO, UUID movieId, UUID personId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, movieId));
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException(Person.class, personId));

        MovieCast movieCast = translationService.toEntity(createDTO);

        movieCast.setMovie(movie);
        movieCast.setPerson(person);
        movieCast = movieCastRepository.save(movieCast);

        return translationService.toRead(movieCast);
    }

    public MovieCastReadDTO updateMovieCast(MovieCastPutDTO updateDTO, UUID id) {
        return null;
    }

    public MovieCastReadDTO patchMovieCast(MovieCastPatchDTO patchDTO, UUID id) {
        return null;
    }

    public void deleteMovieCast(UUID id) {
        movieCastRepository.delete(getMovieCastRequired(id));
    }

    private MovieCast getMovieCastRequired(UUID id) {
        return movieCastRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(MovieCast.class, id));
    }
}
