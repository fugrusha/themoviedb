package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCrewRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovieCrewService {

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public List<MovieCrewReadDTO> getAllMovieCrews(UUID movieId) {
        List<MovieCrew> movieCrews = movieCrewRepository.findByMovieId(movieId);
        return movieCrews.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public MovieCrewReadDTO getMovieCrew(UUID movieId, UUID id) {
        return translationService.toRead(getMovieCrewByMovieIdRequired(id, movieId));
    }

    @Transactional
    public MovieCrewReadExtendedDTO getExtendedMovieCrew(UUID movieId, UUID id) {
        return translationService.toReadExtended(getMovieCrewByMovieIdRequired(id, movieId));
    }

    public MovieCrewReadDTO createMovieCrew(MovieCrewCreateDTO createDTO, UUID movieId) {
        MovieCrew movieCrew = translationService.toEntity(createDTO);
        movieCrew.setMovie(repoHelper.getReferenceIfExist(Movie.class, movieId));

        movieCrew = movieCrewRepository.save(movieCrew);
        return translationService.toRead(movieCrew);
    }

    public MovieCrewReadDTO patchMovieCrew(UUID movieId, UUID id, MovieCrewPatchDTO patchDTO) {
        MovieCrew movieCrew = getMovieCrewByMovieIdRequired(id, movieId);

        translationService.patchEntity(patchDTO, movieCrew);

        movieCrew = movieCrewRepository.save(movieCrew);
        return translationService.toRead(movieCrew);
    }

    public MovieCrewReadDTO updateMovieCrew(UUID movieId, UUID id, MovieCrewPutDTO updateDTO) {
        MovieCrew movieCrew = getMovieCrewByMovieIdRequired(id, movieId);

        translationService.updateEntity(updateDTO, movieCrew);

        movieCrew = movieCrewRepository.save(movieCrew);
        return translationService.toRead(movieCrew);
    }

    public void deleteMovieCrew(UUID movieId, UUID id) {
        movieCrewRepository.delete(getMovieCrewByMovieIdRequired(id, movieId));
    }

    private MovieCrew getMovieCrewByMovieIdRequired(UUID id, UUID movieId) {
        MovieCrew moviePart = movieCrewRepository.findByIdAndMovieId(id, movieId);

        if (moviePart != null) {
            return moviePart;
        } else {
            throw new EntityNotFoundException(MovieCrew.class, id, Movie.class, movieId);
        }
    }
}