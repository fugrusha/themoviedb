package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.service.MovieCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/movie-casts")
public class MovieCastController {

    @Autowired
    private MovieCastService movieCastService;

    @GetMapping
    public PageResult<MovieCastReadDTO> getListOfMovieCast(@PathVariable UUID movieId, Pageable pageable) {
        return movieCastService.getAllMovieCasts(movieId, pageable);
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
            @RequestBody @Valid MovieCastCreateDTO createDTO,
            @PathVariable UUID movieId
    ) {
        return movieCastService.createMovieCast(createDTO, movieId);
    }

    @PutMapping("/{id}")
    public MovieCastReadDTO updateMovieCast(
            @RequestBody @Valid MovieCastPutDTO updateDTO,
            @PathVariable UUID id,
            @PathVariable UUID movieId
    ) {
        return movieCastService.updateMovieCast(updateDTO, id, movieId);
    }

    @PatchMapping("/{id}")
    public MovieCastReadDTO patchMovieCast(
            @RequestBody @Valid MovieCastPatchDTO patchDTO,
            @PathVariable UUID id,
            @PathVariable UUID movieId) {
        return movieCastService.patchMovieCast(patchDTO, id, movieId);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCast(@PathVariable UUID id, @PathVariable UUID movieId) {
        movieCastService.deleteMovieCast(id, movieId);
    }
}
