package com.golovko.backend.service;

import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastPatchDTO;
import com.golovko.backend.dto.moviecast.MovieCastPutDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCastRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MovieCastService {

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private TranslationService translationService;

    public MovieCastReadDTO getMovieCast(UUID id) {
        return translationService.toRead(getMovieCastRequired(id));
    }

    public MovieCastReadDTO createMovieCast(MovieCastCreateDTO createDTO, UUID movieId, UUID personId) {
        return null;
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
