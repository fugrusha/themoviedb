package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movieparticipation.*;
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
        MovieParticipation movieParticipation = getMovieParticipationRequired(id);

        translationService.patchEntity(patchDTO, movieParticipation);

        // TODO How can I remove this branches?
        if (patchDTO.getMovieId() != null) {
            Movie movie = movieRepository.findById(patchDTO.getMovieId())
                    .orElseThrow(() -> new EntityNotFoundException(Movie.class, patchDTO.getMovieId()));
            movieParticipation.setMovie(movie);
        }

        if (patchDTO.getPersonId() != null) {
            Person person = personRepository.findById(patchDTO.getPersonId())
                    .orElseThrow(() -> new EntityNotFoundException(Person.class, patchDTO.getPersonId()));
            movieParticipation.setPerson(person);
        }

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return translationService.toRead(movieParticipation);
    }


    public MoviePartReadDTO updateMovieParticipation(UUID id, MoviePartPutDTO updateDTO) {
        Movie movie = movieRepository.findById(updateDTO.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, updateDTO.getMovieId()));
        Person person = personRepository.findById(updateDTO.getPersonId())
                .orElseThrow(() -> new EntityNotFoundException(Person.class, updateDTO.getPersonId()));

        MovieParticipation movieParticipation = getMovieParticipationRequired(id);

        translationService.updateEntity(updateDTO, movieParticipation);

        movieParticipation.setMovie(movie);
        movieParticipation.setPerson(person);

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return translationService.toRead(movieParticipation);
    }

    public void deleteMovieParticipation(UUID id) {
        movieParticipationRepository.delete(getMovieParticipationRequired(id));
    }

    public MovieParticipation getMovieParticipationRequired(UUID id) {
        return movieParticipationRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(MovieParticipation.class, id));
    }
}
