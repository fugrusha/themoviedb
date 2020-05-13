package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieReadExtendedDTO;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.repository.RepositoryHelper;
import com.golovko.backend.service.importer.MovieImporterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class ContentManagerService {

    @Autowired
    private MovieImporterService movieImporterService;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private TranslationService translationService;

    @Transactional
    public MovieReadExtendedDTO importMovie(String externalMovieId) {
        try {
            UUID importedMovieId = movieImporterService.importMovie(externalMovieId);

            Movie importedMovie = repoHelper.getEntityById(Movie.class, importedMovieId);

            return translationService.translate(importedMovie, MovieReadExtendedDTO.class);

        } catch (ImportedEntityAlreadyExistsException | ImportAlreadyPerformedException ex) {
            log.info("Can't import movie id={}: {}", externalMovieId, ex.getMessage());
        } catch (Exception ex) {
            log.warn("Failed to import movie id={}. {}", externalMovieId, ex.getMessage());
        }

        return null;
    }
}
