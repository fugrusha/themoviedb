package com.golovko.backend.dto.movie;

import com.golovko.backend.domain.PartType;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class MovieFilter {
    private UUID personId;
    private Set<PartType> partTypes;
    private LocalDate releasedFrom;
    private LocalDate releasedTo;
}
