package com.golovko.backend.controller;

import com.golovko.backend.dto.watchlist.*;
import com.golovko.backend.service.WatchlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/watchlists")
public class WatchlistController {

    @Autowired
    private WatchlistService watchlistService;

    @GetMapping
    public List<WatchlistReadDTO> getAllUserWatchlists(@PathVariable UUID userId) {
        return watchlistService.getAllUserWatchlists(userId);
    }

    @GetMapping("/{id}")
    public WatchlistReadDTO getUserWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id
    ) {
        return watchlistService.getUserWatchlist(userId, id);
    }

    @GetMapping("/{id}/extended")
    public WatchlistReadExtendedDTO getUserWatchlistExtended(
            @PathVariable UUID userId,
            @PathVariable UUID id
    ) {
        return watchlistService.getUserWatchlistExtended(userId, id);
    }

    @PostMapping
    public WatchlistReadDTO createWatchlist(
            @PathVariable UUID userId,
            @RequestBody @Valid WatchlistCreateDTO createDTO
    ) {
        return watchlistService.createWatchlist(userId, createDTO);
    }

    @PutMapping("/{id}")
    public WatchlistReadDTO updateWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid WatchlistPutDTO putDTO
    ) {
        return watchlistService.updateWatchlist(userId, id, putDTO);
    }

    @PatchMapping("/{id}")
    public WatchlistReadDTO patchWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid WatchlistPatchDTO patchDTO
    ) {
        return watchlistService.patchWatchlist(userId, id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id
    ) {
        watchlistService.deleteWatchlist(userId, id);
    }
}
