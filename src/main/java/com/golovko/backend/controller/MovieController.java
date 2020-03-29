package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    public PageResult<MovieReadDTO> getMovies(MovieFilter filter, Pageable pageable) {
        return movieService.getMovies(filter, pageable);
    }

    @GetMapping("/{id}")
    public MovieReadDTO getMovieById(@PathVariable UUID id) {
        return movieService.getMovie(id);
    }

    @GetMapping("/{id}/extended")
    public MovieReadExtendedDTO getMovieExtendedById(@PathVariable UUID id) {
        return movieService.getMovieExtended(id);
    }

    @PostMapping
    public MovieReadDTO createMovie(@RequestBody @Valid MovieCreateDTO createDTO) {
        return movieService.createMovie(createDTO);
    }

    @PatchMapping("/{id}")
    public MovieReadDTO patchMovie(@PathVariable UUID id, @RequestBody @Valid MoviePatchDTO patchDTO) {
        return movieService.patchMovie(id, patchDTO);
    }

    @PutMapping("/{id}")
    public MovieReadDTO updateMovie(@PathVariable UUID id, @RequestBody @Valid MoviePutDTO updateDTO) {
        return movieService.updateMovie(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
    }
}
