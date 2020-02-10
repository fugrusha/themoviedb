package com.golovko.backend.service;

import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCastRepository;
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
        MovieCast movieCast = translationService.toEntity(createDTO, personId, movieId);

        movieCast = movieCastRepository.save(movieCast);
        return translationService.toRead(movieCast);
    }

    public MovieCastReadDTO updateMovieCast(MovieCastPutDTO updateDTO, UUID id, UUID movieId) {
        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        translationService.updateEntity(updateDTO, movieCast);

        movieCast = movieCastRepository.save(movieCast);
        return translationService.toRead(movieCast);
    }

    public MovieCastReadDTO patchMovieCast(MovieCastPatchDTO patchDTO, UUID id, UUID movieId) {
        MovieCast movieCast = getMovieCastByMovieIdRequired(id, movieId);

        translationService.patchEntity(patchDTO, movieCast);

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
