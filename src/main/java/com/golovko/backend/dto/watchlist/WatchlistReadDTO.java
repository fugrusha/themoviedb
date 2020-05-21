package com.golovko.backend.dto.watchlist;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class WatchlistReadDTO {

    private UUID id;

    private String name;

    private UUID authorId;

    private Instant createdAt;

    private Instant updatedAt;
}
