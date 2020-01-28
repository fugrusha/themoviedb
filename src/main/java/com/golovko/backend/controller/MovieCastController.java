package com.golovko.backend.controller;

import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastPatchDTO;
import com.golovko.backend.dto.moviecast.MovieCastPutDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.service.MovieCastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movie-cast")
public class MovieCastController {

    @Autowired
    private MovieCastService movieCastService;

    @GetMapping("/{id}")
    public MovieCastReadDTO getMovieCast(@PathVariable UUID id) {
        return movieCastService.getMovieCast(id);
    }

    @PostMapping
    public MovieCastReadDTO createMovieCast(
            @RequestBody MovieCastCreateDTO createDTO,
            @PathVariable UUID movieId,
            @PathVariable UUID personId) {
        return movieCastService.createMovieCast(createDTO, movieId, personId);
    }

    @PutMapping("/{id}")
    public MovieCastReadDTO updateMovieCast(@RequestBody MovieCastPutDTO updateDTO, @PathVariable UUID id) {
        return movieCastService.updateMovieCast(updateDTO, id);
    }

    @PatchMapping("/{id}")
    public MovieCastReadDTO patchMovieCast(@RequestBody MovieCastPatchDTO patchDTO, @PathVariable UUID id) {
        return movieCastService.patchMovieCast(patchDTO, id);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCast(@PathVariable UUID id) {
        movieCastService.deleteMovieCast(id);
    }
}
