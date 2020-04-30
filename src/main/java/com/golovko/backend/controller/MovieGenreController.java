package com.golovko.backend.controller;

import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.service.MovieGenreService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MovieGenreController {

    @Autowired
    private MovieGenreService movieGenreService;

    @ApiOperation(value = "Get all genres for movie")
    @GetMapping("/movies/{movieId}/genres")
    public List<GenreReadDTO> getGenresByMovieId(@PathVariable UUID movieId) {
        return movieGenreService.getMovieGenres(movieId);
    }

    @ApiOperation(value = "Add genre to movie", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @PostMapping("/movies/{movieId}/genres/{id}")
    public List<GenreReadDTO> addGenreToMovie(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieGenreService.addGenreToMovie(movieId, id);
    }

    @ApiOperation(value = "Remove genre to movie", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @DeleteMapping("/movies/{movieId}/genres/{id}")
    public List<GenreReadDTO> removeGenreFromMovie(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieGenreService.removeGenreFromMovie(movieId, id);
    }
}
