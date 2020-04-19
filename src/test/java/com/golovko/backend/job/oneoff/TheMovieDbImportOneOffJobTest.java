package com.golovko.backend.job.oneoff;

import com.golovko.backend.BaseTest;
import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.MovieReadShortDTO;
import com.golovko.backend.client.themoviedb.dto.MoviesPageDTO;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.service.importer.MovieImporterService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

public class TheMovieDbImportOneOffJobTest extends BaseTest {

    @Autowired
    private TheMovieDbImportOneOffJob job;

    @MockBean
    private TheMovieDbClient theMovieDbClient;

    @MockBean
    private MovieImporterService movieImporterService;

    @Test
    public void testDoImport() throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        MoviesPageDTO pageDTO = createPageWith2Results();

        Mockito.when(theMovieDbClient.getTopRatedMovies()).thenReturn(pageDTO);

        job.doImport();

        for (MovieReadShortDTO dto : pageDTO.getResults()) {
            Mockito.verify(movieImporterService).importMovie(dto.getId());
        }
    }

    @Test
    public void testDoImportNoExceptionIfGetPageFailed() {
        Mockito.when(theMovieDbClient.getTopRatedMovies()).thenThrow(RuntimeException.class);

        job.doImport();

        Mockito.verifyNoInteractions(movieImporterService);
    }

    @Test
    public void testDoImportFirstFailedAndSecondSuccess()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        MoviesPageDTO pageDTO = createPageWith2Results();

        Mockito.when(theMovieDbClient.getTopRatedMovies()).thenReturn(pageDTO);
        Mockito.when(movieImporterService.importMovie(pageDTO.getResults().get(0).getId()))
                .thenThrow(RuntimeException.class);

        job.doImport();

        for (MovieReadShortDTO dto : pageDTO.getResults()) {
            Mockito.verify(movieImporterService).importMovie(dto.getId());
        }
    }

    private MoviesPageDTO createPageWith2Results() {
        MoviesPageDTO dto = generateObject(MoviesPageDTO.class);
        dto.getResults().add(generateObject(MovieReadShortDTO.class));
        Assert.assertEquals(2, dto.getResults().size());

        return dto;
    }
}
