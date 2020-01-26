package com.golovko.backend.service;

import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.dto.movieParticipation.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieParticipationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MovieParticipationService {

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

    @Autowired
    private TranslationService translationService;

    public MoviePartReadDTO getMovieParticipation(UUID id) {
        return translationService.toRead(getMovieParticipationRequired(id));
    }

    public MoviePartReadExtendedDTO getExtendedMovieParticipation(UUID id) {
        return translationService.toReadExtended(getMovieParticipationRequired(id));
    }

    public MoviePartReadDTO createMovieParticipation(MoviePartCreateDTO createDTO) {
        return null;
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
