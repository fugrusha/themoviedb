package com.golovko.backend.controller;

import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.service.MovieCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{movieId}/movie-cast")
public class MovieCastController {

    @Autowired
    private MovieCastService movieCastService;

    @GetMapping
    public List<MovieCastReadDTO> getListOfMovieCast(@PathVariable UUID movieId) {
        return movieCastService.getListOfMovieCast(movieId);
    }

    @GetMapping("/{id}")
    public MovieCastReadDTO getMovieCast(@PathVariable UUID id, @PathVariable UUID movieId) {
        return movieCastService.getMovieCast(id, movieId);
    }

    @GetMapping("/{id}/extended")
    public MovieCastReadExtendedDTO getMovieCastExtended(@PathVariable UUID id, @PathVariable UUID movieId) {
        return movieCastService.getMovieCastExtended(id, movieId);
    }

    @PostMapping
    public MovieCastReadDTO createMovieCast(
            @RequestBody MovieCastCreateDTO createDTO,
            @RequestParam UUID personId,
            @PathVariable UUID movieId
    ) {
        return movieCastService.createMovieCast(createDTO, personId, movieId);
    }

    @PutMapping("/{id}")
    public MovieCastReadDTO updateMovieCast(
            @RequestBody MovieCastPutDTO updateDTO,
            @PathVariable UUID id,
            @PathVariable UUID movieId
    ) {
        return movieCastService.updateMovieCast(updateDTO, id, movieId);
    }

    @PatchMapping("/{id}")
    public MovieCastReadDTO patchMovieCast(
            @RequestBody MovieCastPatchDTO patchDTO,
            @PathVariable UUID id,
            @PathVariable UUID movieId) {
        return movieCastService.patchMovieCast(patchDTO, id, movieId);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCast(@PathVariable UUID id, @PathVariable UUID movieId) {
        movieCastService.deleteMovieCast(id, movieId);
    }
}
