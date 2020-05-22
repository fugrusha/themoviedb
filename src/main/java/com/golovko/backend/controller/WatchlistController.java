package com.golovko.backend.controller;

import com.golovko.backend.controller.security.CurrentUser;
import com.golovko.backend.dto.watchlist.*;
import com.golovko.backend.service.WatchlistService;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "Get all user's watchlists")
    @GetMapping
    public List<WatchlistReadDTO> getAllUserWatchlists(@PathVariable UUID userId) {
        return watchlistService.getAllUserWatchlists(userId);
    }

    @ApiOperation(value = "Get single user's watchlist")
    @GetMapping("/{id}")
    public WatchlistReadDTO getUserWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id
    ) {
        return watchlistService.getUserWatchlist(userId, id);
    }

    @ApiOperation(value = "Get single extended user's watchlist")
    @GetMapping("/{id}/extended")
    public WatchlistReadExtendedDTO getUserWatchlistExtended(
            @PathVariable UUID userId,
            @PathVariable UUID id
    ) {
        return watchlistService.getUserWatchlistExtended(userId, id);
    }

    @ApiOperation(value = "Create new watchlist", notes = "Needs current user authority")
    @CurrentUser
    @PostMapping
    public WatchlistReadDTO createWatchlist(
            @PathVariable UUID userId,
            @RequestBody @Valid WatchlistCreateDTO createDTO
    ) {
        return watchlistService.createWatchlist(userId, createDTO);
    }

    @ApiOperation(value = "Update watchlist", notes = "Needs current user authority")
    @CurrentUser
    @PutMapping("/{id}")
    public WatchlistReadDTO updateWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid WatchlistPutDTO putDTO
    ) {
        return watchlistService.updateWatchlist(userId, id, putDTO);
    }

    @ApiOperation(value = "Update watchlist", notes = "Needs current user authority")
    @CurrentUser
    @PatchMapping("/{id}")
    public WatchlistReadDTO patchWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid WatchlistPatchDTO patchDTO
    ) {
        return watchlistService.patchWatchlist(userId, id, patchDTO);
    }

    @ApiOperation(value = "Delete watchlist", notes = "Needs current user authority")
    @CurrentUser
    @DeleteMapping("/{id}")
    public void deleteWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id
    ) {
        watchlistService.deleteWatchlist(userId, id);
    }

    @ApiOperation(value = "Add movie to watchlist", notes = "Needs current user authority")
    @CurrentUser
    @PostMapping("/{id}/movies/{movieId}")
    public WatchlistReadDTO addMovieToWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID movieId
    ) {
        return watchlistService.addMovieToWatchlist(userId, id, movieId);
    }

    @ApiOperation(value = "Remove movie from watchlist", notes = "Needs current user authority")
    @CurrentUser
    @DeleteMapping("/{id}/movies/{movieId}")
    public WatchlistReadDTO removeMovieToWatchlist(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID movieId
    ) {
        return watchlistService.removeMovieFromWatchlist(userId, id, movieId);
    }
}
