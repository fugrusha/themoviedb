package com.golovko.backend.dto.watchlist;

import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class WatchlistReadExtendedDTO {

    private UUID id;

    private String name;

    private UserReadDTO author;

    private List<MovieReadDTO> movies;

    private Instant createdAt;

    private Instant updatedAt;
}
