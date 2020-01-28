package com.golovko.backend.controller;

import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MoviePatchDTO;
import com.golovko.backend.dto.movie.MoviePutDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/{id}")
    public MovieReadDTO getMovieById(@PathVariable UUID id) {
        return movieService.getMovie(id);
    }

    @PostMapping
    public MovieReadDTO createMovie(@RequestBody MovieCreateDTO createDTO) {
        return movieService.createMovie(createDTO);
    }

    @PatchMapping("/{id}")
    public MovieReadDTO patchMovie(@PathVariable UUID id, @RequestBody MoviePatchDTO patchDTO) {
        return movieService.patchMovie(id, patchDTO);
    }

    @PutMapping("/{id}")
    public MovieReadDTO updateMovie(@PathVariable UUID id, @RequestBody MoviePutDTO updateDTO) {
        return movieService.updateMovie(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable UUID id){
        movieService.deleteMovie(id);
    }

}
