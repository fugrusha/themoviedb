package com.golovko.backend.dto.genre;

import com.golovko.backend.dto.movie.MovieReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class GenreReadExtendedDTO {

    private UUID id;

    private String genreName;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;

    private List<MovieReadDTO> movies;
}
