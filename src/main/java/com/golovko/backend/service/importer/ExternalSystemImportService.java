package com.golovko.backend.service.importer;

import com.golovko.backend.domain.*;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.repository.ExternalSystemImportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExternalSystemImportService {

    @Autowired
    private ExternalSystemImportRepository esiRepository;

    public void validateNotImported(
            Class<? extends AbstractEntity> entityToImport,
            String idInExternalSystem
    ) throws ImportAlreadyPerformedException {
        ImportedEntityType entityType = getImportedEntityType(entityToImport);

        ExternalSystemImport esi = esiRepository.findByIdInExternalSystemAndEntityType(
                idInExternalSystem, entityType);

        if (esi != null) {
            throw new ImportAlreadyPerformedException(esi);
        }
    }

    private ImportedEntityType getImportedEntityType(Class<? extends AbstractEntity> entityToImport) {

        if (Movie.class.equals(entityToImport)) {
            return ImportedEntityType.MOVIE;
        } else if (Person.class.equals(entityToImport)) {
            return ImportedEntityType.PERSON;
        } else if (Genre.class.equals(entityToImport)) {
            return ImportedEntityType.GENRE;
        } else if (MovieCrew.class.equals(entityToImport)) {
            return ImportedEntityType.CREW;
        } else if (MovieCast.class.equals(entityToImport)) {
            return ImportedEntityType.CAST;
        }

        throw new IllegalArgumentException("Importing of " + entityToImport + " entities is not supported");
    }

    public <T extends AbstractEntity> UUID createExternalSystemImport(T entity, String idInExternalSystem) {
        ImportedEntityType entityType = getImportedEntityType(entity.getClass());

        ExternalSystemImport esi = new ExternalSystemImport();
        esi.setEntityType(entityType);
        esi.setEntityId(entity.getId());
        esi.setIdInExternalSystem(idInExternalSystem);
        esi = esiRepository.save(esi);
        return esi.getId();
    }

    public UUID getImportedEntityId(
            Class<? extends AbstractEntity> entityToImport,
            String idInExternalSystem
    ) {
        ImportedEntityType entityType = getImportedEntityType(entityToImport);

        ExternalSystemImport esi = esiRepository.findByIdInExternalSystemAndEntityType(
                idInExternalSystem, entityType);

        if (esi != null) {
            return esi.getEntityId();
        } else {
            return null;
        }
    }
}
