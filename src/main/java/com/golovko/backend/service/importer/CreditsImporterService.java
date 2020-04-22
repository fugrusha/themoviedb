package com.golovko.backend.service.importer;

import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.CastReadDTO;
import com.golovko.backend.client.themoviedb.dto.CrewReadDTO;
import com.golovko.backend.client.themoviedb.dto.MovieCreditsReadDTO;
import com.golovko.backend.domain.*;
import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.repository.MovieCrewRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class CreditsImporterService {

    @Autowired
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private ExternalSystemImportService externalSystemImportService;

    @Autowired
    private PersonImporterService personImporterService;

    @Transactional
    public void importMovieCredits(String externalMovieId, Movie movie) {
        log.info("Importing crews and casts for movie with external id={}", externalMovieId);

        MovieCreditsReadDTO readDTO = theMovieDbClient.getMovieCastAndCrew(externalMovieId, null);

        if (CollectionUtils.isNotEmpty(readDTO.getCast())) {
            importCasts(readDTO.getCast(), movie);
        }
        if (CollectionUtils.isNotEmpty(readDTO.getCrew())) {
            importCrews(readDTO.getCrew(), movie);
        }

        log.info("Import credits finished for movie with external id={}", externalMovieId);
    }

    private void importCrews(List<CrewReadDTO> crews, Movie movie) {
        Set<MovieCrew> movieCrews = new HashSet<>();

        for (CrewReadDTO crewDTO : crews) {
            try {
                MovieCrew mc = createMovieCrew(movie, crewDTO);
                externalSystemImportService.createExternalSystemImport(mc, crewDTO.getCreditId());
                movieCrews.add(mc);
            } catch (Exception e) {
                log.error("Failed to import person with id={}: {}", crewDTO.getPersonId(), e.getMessage());
            }
        }

        movie.getMovieCrews().addAll(movieCrews);
        log.info("Imported {} crew members for movie: {}", movieCrews.size(), movie.getMovieTitle());
    }

    private void importCasts(List<CastReadDTO> casts, Movie movie) {
        Set<MovieCast> movieCasts = new HashSet<>();

        for (CastReadDTO castDTO : casts) {
            try {
                MovieCast mc = createMovieCast(movie, castDTO);
                externalSystemImportService.createExternalSystemImport(mc, castDTO.getCastId());
                movieCasts.add(mc);
            } catch (Exception e) {
                log.error("Failed to import person with id={}: {}", castDTO.getPersonId(), e.getMessage());
            }
        }

        movie.getMovieCasts().addAll(movieCasts);
        log.info("Imported {} cast members for movie: {}", movieCasts.size(), movie.getMovieTitle());
    }


    private MovieCrew createMovieCrew(Movie movie, CrewReadDTO crewDTO) {
        MovieCrew mc = new MovieCrew();
        mc.setMovie(movie);
        mc.setPerson(personImporterService.importPersonIfNeeded(crewDTO.getPersonId()));
        mc.setDescription(crewDTO.getJob());
        mc.setMovieCrewType(getMovieCrewType(crewDTO.getDepartment()));
        movieCrewRepository.save(mc);
        return mc;
    }

    private MovieCast createMovieCast(Movie movie, CastReadDTO castDTO) {
        MovieCast mc = new MovieCast();
        mc.setMovie(movie);
        mc.setPerson(personImporterService.importPersonIfNeeded(castDTO.getPersonId()));
        mc.setMovieCrewType(MovieCrewType.CAST);
        mc.setCharacter(castDTO.getCharacter());
        mc.setOrderNumber(castDTO.getOrder());

        if (castDTO.getGender().equals(1)) {
            mc.setGender(Gender.FEMALE);
        } else if (castDTO.getGender().equals(2)) {
            mc.setGender(Gender.MALE);
        } else {
            mc.setGender(Gender.UNDEFINED);
        }

        movieCastRepository.save(mc);
        return mc;
    }

    private MovieCrewType getMovieCrewType(String department) {

        if (department.equalsIgnoreCase("editing")) {
            return MovieCrewType.EDITOR;
        } else if (department.equalsIgnoreCase("camera")) {
            return MovieCrewType.CAMERA;
        } else if (department.contains("Costume")) {
            return MovieCrewType.COSTUME_DESIGNER;
        } else if (department.equalsIgnoreCase("sound")) {
            return MovieCrewType.SOUND;
        } else if (department.equalsIgnoreCase("lighting")) {
            return MovieCrewType.LIGHTING;
        } else if (department.equalsIgnoreCase("art")) {
            return MovieCrewType.ART;
        } else if (department.equalsIgnoreCase("directing")) {
            return MovieCrewType.DIRECTOR;
        } else if (department.equalsIgnoreCase("crew")) {
            return MovieCrewType.CREW;
        } else if (department.equalsIgnoreCase("production")) {
            return MovieCrewType.PRODUCER;
        } else if (department.equalsIgnoreCase("Visual Effects")) {
            return MovieCrewType.VISUAL_EFFECTS;
        } else if (department.equalsIgnoreCase("writing")) {
            return MovieCrewType.WRITER;
        }

        throw new IllegalArgumentException("Importing of " + department
                + " job for movie credits is not supported");
    }
}
