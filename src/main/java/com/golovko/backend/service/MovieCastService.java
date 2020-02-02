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
        Movie movie = movieRepository.findById(updateDTO.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, updateDTO.getMovieId()));
        Person person = personRepository.findById(updateDTO.getPersonId())
                .orElseThrow(() -> new EntityNotFoundException(Person.class, updateDTO.getPersonId()));

        MovieCast movieCast = getMovieCastRequired(id);

        translationService.updateEntity(updateDTO, movieCast);

        movieCast.setMovie(movie);
        movieCast.setPerson(person);

        movieCast = movieCastRepository.save(movieCast);
        return translationService.toRead(movieCast);
    }

    public MovieCastReadDTO patchMovieCast(MovieCastPatchDTO patchDTO, UUID id) {
        MovieCast movieCast = getMovieCastRequired(id);

        translationService.patchEntity(patchDTO, movieCast);

        // TODO How can I remove this branches?
        if (patchDTO.getMovieId() != null) {
            Movie movie = movieRepository.findById(patchDTO.getMovieId())
                    .orElseThrow(() -> new EntityNotFoundException(Movie.class, patchDTO.getMovieId()));
            movieCast.setMovie(movie);
        }

        if (patchDTO.getPersonId() != null) {
            Person person = personRepository.findById(patchDTO.getPersonId())
                    .orElseThrow(() -> new EntityNotFoundException(Person.class, patchDTO.getPersonId()));
            movieCast.setPerson(person);
        }

        movieCast = movieCastRepository.save(movieCast);
        return translationService.toRead(movieCast);
    }

    public void deleteMovieCast(UUID id) {
        movieCastRepository.delete(getMovieCastRequired(id));
    }

    private MovieCast getMovieCastRequired(UUID id) {
        return movieCastRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(MovieCast.class, id));
    }
}
