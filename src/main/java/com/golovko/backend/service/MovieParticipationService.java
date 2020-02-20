package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.dto.movieparticipation.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieParticipationRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovieParticipationService {

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public List<MoviePartReadDTO> getListOfMovieParticipation(UUID movieId) {
        List<MovieParticipation> listOfMoviePart = movieParticipationRepository.findByMovieId(movieId);
        return translationService.toReadListOfMoviePart(listOfMoviePart);
    }

    public MoviePartReadDTO getMovieParticipation(UUID movieId, UUID id) {
        return translationService.toRead(getMovieParticipationByMovieIdRequired(id, movieId));
    }

    @Transactional
    public MoviePartReadExtendedDTO getExtendedMovieParticipation(UUID movieId, UUID id) {
        return translationService.toReadExtended(getMovieParticipationByMovieIdRequired(id, movieId));
    }

    public MoviePartReadDTO createMovieParticipation(MoviePartCreateDTO createDTO, UUID movieId) {
        MovieParticipation movieParticipation = translationService.toEntity(createDTO);
        movieParticipation.setMovie(repoHelper.getReferenceIfExist(Movie.class, movieId));

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return translationService.toRead(movieParticipation);
    }

    public MoviePartReadDTO patchMovieParticipation(UUID movieId, UUID id, MoviePartPatchDTO patchDTO) {
        MovieParticipation movieParticipation = getMovieParticipationByMovieIdRequired(id, movieId);

        translationService.patchEntity(patchDTO, movieParticipation);

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return translationService.toRead(movieParticipation);
    }

    public MoviePartReadDTO updateMovieParticipation(UUID movieId, UUID id, MoviePartPutDTO updateDTO) {
        MovieParticipation movieParticipation = getMovieParticipationByMovieIdRequired(id, movieId);

        translationService.updateEntity(updateDTO, movieParticipation);

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return translationService.toRead(movieParticipation);
    }

    public void deleteMovieParticipation(UUID movieId, UUID id) {
        movieParticipationRepository.delete(getMovieParticipationByMovieIdRequired(id, movieId));
    }

    private MovieParticipation getMovieParticipationByMovieIdRequired(UUID id, UUID movieId) {
        if (movieParticipationRepository.findByIdAndMovieId(id, movieId) != null) {
            return movieParticipationRepository.findByIdAndMovieId(id, movieId);
        } else {
            throw new EntityNotFoundException(MovieParticipation.class, id, Movie.class, movieId);
        }
    }
}
