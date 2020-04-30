package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.service.MovieCastService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/movie-casts")
public class MovieCastController {

    @Autowired
    private MovieCastService movieCastService;

    @ApiPageable
    @ApiOperation(value = "Get all movieCasts for movie")
    @GetMapping
    public PageResult<MovieCastReadDTO> getAllMovieCasts(
            @PathVariable UUID movieId,
            @ApiIgnore Pageable pageable
    ) {
        return movieCastService.getAllMovieCasts(movieId, pageable);
    }

    @ApiOperation(value = "Get single movieCast for movie")
    @GetMapping("/{id}")
    public MovieCastReadDTO getMovieCast(@PathVariable UUID id, @PathVariable UUID movieId) {
        return movieCastService.getMovieCast(id, movieId);
    }

    @ApiOperation(value = "Get extended movieCast for movie")
    @GetMapping("/{id}/extended")
    public MovieCastReadExtendedDTO getMovieCastExtended(@PathVariable UUID id, @PathVariable UUID movieId) {
        return movieCastService.getMovieCastExtended(id, movieId);
    }

    @ApiOperation(value = "Create movieCast", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @PostMapping
    public MovieCastReadDTO createMovieCast(
            @RequestBody @Valid MovieCastCreateDTO createDTO,
            @PathVariable UUID movieId
    ) {
        return movieCastService.createMovieCast(createDTO, movieId);
    }

    @ApiOperation(value = "Update movieCast",
            notes = "Needs CONTENT_MANAGER authority. All fields will bew updated")
    @ContentManager
    @PutMapping("/{id}")
    public MovieCastReadDTO updateMovieCast(
            @RequestBody @Valid MovieCastPutDTO updateDTO,
            @PathVariable UUID id,
            @PathVariable UUID movieId
    ) {
        return movieCastService.updateMovieCast(updateDTO, id, movieId);
    }

    @ApiOperation(value = "Update movieCast",
            notes = "Needs CONTENT_MANAGER authority. Empty fields will not be updated")
    @ContentManager
    @PatchMapping("/{id}")
    public MovieCastReadDTO patchMovieCast(
            @RequestBody @Valid MovieCastPatchDTO patchDTO,
            @PathVariable UUID id,
            @PathVariable UUID movieId) {
        return movieCastService.patchMovieCast(patchDTO, id, movieId);
    }

    @ApiOperation(value = "Delete movieCast", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @DeleteMapping("/{id}")
    public void deleteMovieCast(@PathVariable UUID id, @PathVariable UUID movieId) {
        movieCastService.deleteMovieCast(id, movieId);
    }
}
