package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieReadExtendedDTO;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.service.importer.MovieImporterService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class ContentManagerServiceTest extends BaseTest {

    @Autowired
    private ContentManagerService contentManagerService;

    @MockBean
    private MovieImporterService movieImporterService;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testIImportMovie()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        Mockito.when(movieImporterService.importMovie(externalMovieId)).thenReturn(movie.getId());

        MovieReadExtendedDTO importedMovieReadDTO = contentManagerService.importMovie(externalMovieId);

        Movie importedMovie = movieRepository.findById(importedMovieReadDTO.getId()).get();
        Assert.assertEquals(movie.getMovieTitle(), importedMovie.getMovieTitle());
    }

    @Test
    public void testImportAlreadyExists()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        Mockito.when(movieImporterService.importMovie(externalMovieId))
                .thenThrow(ImportedEntityAlreadyExistsException.class);

        MovieReadExtendedDTO readDTO = contentManagerService.importMovie(externalMovieId);

        Assert.assertNull(readDTO);
        Assertions.assertThatThrownBy(() -> movieImporterService.importMovie(externalMovieId))
                .isInstanceOf(ImportedEntityAlreadyExistsException.class);
    }

    @Test
    public void testDuplicateImport()
            throws ImportAlreadyPerformedException, ImportedEntityAlreadyExistsException {
        String externalMovieId = "id200";

        Mockito.when(movieImporterService.importMovie(externalMovieId))
                .thenThrow(ImportAlreadyPerformedException.class);

        MovieReadExtendedDTO readDTO = contentManagerService.importMovie(externalMovieId);

        Assert.assertNull(readDTO);
        Assertions.assertThatThrownBy(() -> movieImporterService.importMovie(externalMovieId))
                .isInstanceOf(ImportAlreadyPerformedException.class);
    }
}
