package com.golovko.backend.repository;

import com.golovko.backend.domain.ExternalSystemImport;
import com.golovko.backend.domain.ImportedEntityType;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ExternalSystemImportRepository extends CrudRepository<ExternalSystemImport, UUID> {

    ExternalSystemImport findByIdInExternalSystemAndEntityType(String idInExternalSystem,
                                                               ImportedEntityType entityType);
}
