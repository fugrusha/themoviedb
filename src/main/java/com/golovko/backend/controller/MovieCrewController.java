package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.service.MovieCrewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/movie-crews")
public class MovieCrewController {

    @Autowired
    private MovieCrewService movieCrewService;

    @GetMapping
    public PageResult<MovieCrewReadDTO> getAllMovieCrews(@PathVariable UUID movieId, Pageable pageable) {
        return movieCrewService.getAllMovieCrews(movieId, pageable);
    }

    @GetMapping("/{id}")
    public MovieCrewReadDTO getMovieCrew(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieCrewService.getMovieCrew(movieId, id);
    }

    @GetMapping("/{id}/extended")
    public MovieCrewReadExtendedDTO getExtendedMovieCrew(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieCrewService.getExtendedMovieCrew(movieId, id);
    }

    @PostMapping
    public MovieCrewReadDTO createMovieCrew(
            @RequestBody @Valid MovieCrewCreateDTO createDTO,
            @PathVariable UUID movieId
    ) {
        return movieCrewService.createMovieCrew(createDTO, movieId);
    }

    @PatchMapping("/{id}")
    public MovieCrewReadDTO patchMovieCrew(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid MovieCrewPatchDTO patchDTO
    ) {
        return movieCrewService.patchMovieCrew(movieId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public MovieCrewReadDTO updateMovieCrew(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid MovieCrewPutDTO updateDTO
    ) {
        return movieCrewService.updateMovieCrew(movieId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCrew(@PathVariable UUID movieId, @PathVariable UUID id) {
        movieCrewService.deleteMovieCrew(movieId, id);
    }
}
