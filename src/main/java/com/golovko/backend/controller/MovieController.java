package com.golovko.backend.controller;

import com.golovko.backend.dto.MovieCreateDTO;
import com.golovko.backend.dto.MovieReadDTO;
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

}
