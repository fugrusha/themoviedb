package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.service.MovieService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @ApiPageable
    @ApiOperation(value = "Get all movies by filter")
    @GetMapping
    public PageResult<MovieReadDTO> getMovies(MovieFilter filter, @ApiIgnore Pageable pageable) {
        return movieService.getMovies(filter, pageable);
    }

    // TODO getLatest
    // TODO getUpcoming

    @ApiPageable
    @ApiOperation(value = "Get top rated movies", notes = "Sorted by average rating")
    @GetMapping("/top-rated")
    public PageResult<MoviesTopRatedDTO> getTopRatedMovies(@ApiIgnore Pageable pageable) {
        return movieService.getTopRatedMovies(pageable);
    }

    @ApiOperation(value = "Get single movie")
    @GetMapping("/{id}")
    public MovieReadDTO getMovieById(@PathVariable UUID id) {
        return movieService.getMovie(id);
    }

    @ApiOperation(value = "Get movie details")
    @GetMapping("/{id}/extended")
    public MovieReadExtendedDTO getMovieExtendedById(@PathVariable UUID id) {
        return movieService.getMovieExtended(id);
    }

    @ApiOperation(value = "Get all movies by filter")
    @ContentManager
    @PostMapping
    public MovieReadDTO createMovie(@RequestBody @Valid MovieCreateDTO createDTO) {
        return movieService.createMovie(createDTO);
    }

    @ApiOperation(value = "Update movie",
            notes = "Needs CONTENT_MANAGER authority. Empty fields will not be updated")
    @ContentManager
    @PatchMapping("/{id}")
    public MovieReadDTO patchMovie(@PathVariable UUID id, @RequestBody @Valid MoviePatchDTO patchDTO) {
        return movieService.patchMovie(id, patchDTO);
    }

    @ApiOperation(value = "Update movie",
            notes = "Needs CONTENT_MANAGER authority. All fields will be updated")
    @ContentManager
    @PutMapping("/{id}")
    public MovieReadDTO updateMovie(@PathVariable UUID id, @RequestBody @Valid MoviePutDTO updateDTO) {
        return movieService.updateMovie(id, updateDTO);
    }

    @ApiOperation(value = "Delete movie", notes = "Needs CONTENT_MANAGER authority.")
    @ContentManager
    @DeleteMapping("/{id}")
    public void deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
    }
}
