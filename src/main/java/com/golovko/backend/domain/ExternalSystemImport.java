package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.UUID;

@Entity
@Getter
@Setter
public class ExternalSystemImport extends AbstractEntity {

    @Enumerated(EnumType.STRING)
    private ImportedEntityType entityType;

    private UUID entityId;

    private String idInExternalSystem;
}
