package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ExternalSystemImport;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static com.golovko.backend.domain.ImportedEntityType.MOVIE;
import static com.golovko.backend.domain.ImportedEntityType.PERSON;

public class ExternalSystemImportRepositoryTest extends BaseTest {

    @Autowired
    private ExternalSystemImportRepository esiRepository;

    @Test
    public void testCreatedAtIsSet() {
        ExternalSystemImport esi = testObjectFactory.createESI("id", MOVIE);

        Instant createdAtBeforeReload = esi.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        esi = esiRepository.findById(esi.getId()).get();

        Instant createdAtAfterReload = esi.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ExternalSystemImport esi = testObjectFactory.createESI("id", MOVIE);

        Instant modifiedAtBeforeReload = esi.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        esi = esiRepository.findById(esi.getId()).get();
        esi.setEntityId(UUID.randomUUID());
        esi = esiRepository.save(esi);
        Instant modifiedAtAfterReload = esi.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testFindByIdInExternalSystemAndEntityType() {
        ExternalSystemImport esi = testObjectFactory.createESI("id1", MOVIE);
        testObjectFactory.createESI("id1", PERSON);
        testObjectFactory.createESI("id2", MOVIE);

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(esi.getIdInExternalSystem(), MOVIE);

        Assert.assertEquals(esi.getIdInExternalSystem(), actualEsi.getIdInExternalSystem());
    }
}
