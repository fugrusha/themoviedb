package com.golovko.backend.exception;

import com.golovko.backend.domain.ExternalSystemImport;
import lombok.Getter;

@Getter
public class ImportAlreadyPerformedException extends Exception {

    private ExternalSystemImport esi;

    public ImportAlreadyPerformedException(ExternalSystemImport esi) {
        super(String.format("Already performed import of %s with id=%s and id in external system = %s",
                esi.getEntityType(), esi.getEntityId(), esi.getIdInExternalSystem()));

        this.esi = esi;
    }
}
