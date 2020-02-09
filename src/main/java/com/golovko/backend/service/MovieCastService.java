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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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


    public List<MovieCastReadDTO> getListOfMovieCast(UUID movieId) {
        List<MovieCast> listOfMovieCast = movieCastRepository.findByMovieId(movieId);
        return translationService.toReadList(listOfMovieCast);
    }

    public MovieCastReadDTO getMovieCast(UUID id, UUID movieId) {
        return translationService.toRead(getMovieCastByMovieIdRequired(id, movieId));
    }

    @Transactional
    public MovieCastReadExtendedDTO getMovieCastExtended(UUID id, UUID movieId) {
        return translationService.toReadExtended(getMovieCastByMovieIdRequired(id, movieId));
    }

    public MovieCastReadDTO createMovieCast(MovieCastCreateDTO createDTO, UUID personId, UUID movieId) {
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

    public MovieCastReadDTO updateMovieCast(MovieCastPutDTO updateDTO, UUID id, UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, movieId));
        Person person = personRepository.findById(updateDTO.getPersonId())
                .orElseThrow(() -> new EntityNotFoundException(Person.class, updateDTO.getPersonId()));

        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        translationService.updateEntity(updateDTO, movieCast);

        movieCast.setMovie(movie);
        movieCast.setPerson(person);

        movieCast = movieCastRepository.save(movieCast);
        return translationService.toRead(movieCast);
    }

    public MovieCastReadDTO patchMovieCast(MovieCastPatchDTO patchDTO, UUID id, UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, movieId));

        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        if (patchDTO.getPersonId() != null) {
            Person person = personRepository.findById(patchDTO.getPersonId())
                    .orElseThrow(() -> new EntityNotFoundException(Person.class, patchDTO.getPersonId()));
            movieCast.setPerson(person);
        }

        translationService.patchEntity(patchDTO, movieCast);

        movieCast.setMovie(movie);

        movieCast = movieCastRepository.save(movieCast);
        return translationService.toRead(movieCast);
    }

    public void deleteMovieCast(UUID id, UUID movieId) {
        movieCastRepository.delete(getMovieCastByMovieIdRequired(id, movieId));
    }

    private MovieCast getMovieCastByMovieIdRequired(UUID id, UUID movieId) {
        if (movieCastRepository.findByIdAndMovieId(id, movieId) != null) {
            return movieCastRepository.findByIdAndMovieId(id, movieId);
        } else {
            throw new EntityNotFoundException(MovieCast.class, id);
        }
    }
}
