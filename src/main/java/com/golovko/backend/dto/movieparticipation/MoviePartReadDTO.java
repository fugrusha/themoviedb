package com.golovko.backend.dto.movieparticipation;

import com.golovko.backend.domain.PartType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MoviePartReadDTO {

    private UUID id;

    private String partInfo;

    private PartType partType;

    private Double averageRating;

    private UUID movieId;

    private UUID personId;

    private Instant createdAt;

    private Instant updatedAt;
}
