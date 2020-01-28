package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movieParticipation.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieParticipationRepository;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MovieParticipationService {

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private TranslationService translationService;

    public MoviePartReadDTO getMovieParticipation(UUID id) {
        return translationService.toRead(getMovieParticipationRequired(id));
    }

    public MoviePartReadExtendedDTO getExtendedMovieParticipation(UUID id) {
        return translationService.toReadExtended(getMovieParticipationRequired(id));
    }

    public MoviePartReadDTO createMovieParticipation(MoviePartCreateDTO createDTO, UUID movieId, UUID personId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, movieId));
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new EntityNotFoundException(Person.class, personId));

        MovieParticipation movieParticipation = translationService.toEntity(createDTO);

        movieParticipation.setMovie(movie);
        movieParticipation.setPerson(person);
        movieParticipation = movieParticipationRepository.save(movieParticipation);

        return translationService.toRead(movieParticipation);
    }

    public MoviePartReadDTO patchMovieParticipation(UUID id, MoviePartPatchDTO patchDTO) {
        return null;
    }


    public MoviePartReadDTO updateMovieParticipation(UUID id, MoviePartUpdateDTO updateDTO) {
        return null;
    }

    public void deleteMovieParticipation(UUID id) {
        movieParticipationRepository.delete(getMovieParticipationRequired(id));
    }

    public MovieParticipation getMovieParticipationRequired(UUID id) {
        return movieParticipationRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(MovieParticipation.class, id));
    }
}
