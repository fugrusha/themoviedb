package com.golovko.backend.dto.genre;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class GenreReadDTO {

    private UUID id;

    private String genreName;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;
}
