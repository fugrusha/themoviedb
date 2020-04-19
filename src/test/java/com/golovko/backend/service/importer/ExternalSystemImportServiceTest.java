package com.golovko.backend.service.importer;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ExternalSystemImport;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.repository.ExternalSystemImportRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static com.golovko.backend.domain.ImportedEntityType.MOVIE;

public class ExternalSystemImportServiceTest extends BaseTest {

    @Autowired
    private ExternalSystemImportService externalSystemImportService;

    @Autowired
    private ExternalSystemImportRepository esiRepository;

    @Test
    public void testValidateNotImported() throws ImportAlreadyPerformedException {
        externalSystemImportService.validateNotImported(Movie.class, "id1");
    }

    @Test
    public void testExceptionWhenAlreadyImported() {
        String idInExternalSystem = "id123";
        ExternalSystemImport esi = testObjectFactory.createESI(idInExternalSystem, MOVIE);

        ImportAlreadyPerformedException ex = Assertions.catchThrowableOfType(
                () -> externalSystemImportService.validateNotImported(Movie.class, idInExternalSystem),
                ImportAlreadyPerformedException.class);

        Assertions.assertThat(ex.getEsi()).isEqualToComparingFieldByField(esi);
    }

    @Test
    public void testNoExceptionWhenAlreadyImportedButDifferentEntityType()
            throws ImportAlreadyPerformedException {
        String idInExternalSystem = "id123";
        testObjectFactory.createESI(idInExternalSystem, MOVIE);

        externalSystemImportService.validateNotImported(Person.class, idInExternalSystem);
    }

    @Test
    public void testCreateExternalSystemImport() {
        Movie movie = testObjectFactory.createMovie();
        String idInExternalSystem = "id123";

        UUID importId = externalSystemImportService.createExternalSystemImport(movie, idInExternalSystem);

        Assert.assertNotNull(importId);

        ExternalSystemImport esi = esiRepository.findById(importId).get();

        Assert.assertEquals(idInExternalSystem, esi.getIdInExternalSystem());
        Assert.assertEquals(MOVIE, esi.getEntityType());
        Assert.assertEquals(movie.getId(), esi.getEntityId());
    }

    @Test
    public void testGetImportedEntityId() {
        Movie movie = testObjectFactory.createMovie();
        String idInExternalSystem = "id123";

        externalSystemImportService.createExternalSystemImport(movie, idInExternalSystem);

        UUID movieId = externalSystemImportService.getImportedEntityId(Movie.class, idInExternalSystem);

        Assert.assertNotNull(movieId);
        Assert.assertEquals(movie.getId(), movieId);
    }

    @Test
    public void testGetImportedEntityIdReturnNull() {
        String idInExternalSystem = "id123";

        UUID movieId = externalSystemImportService.getImportedEntityId(Movie.class, idInExternalSystem);

        Assert.assertNull(movieId);
    }
}
