package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.service.MovieCrewService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/movie-crews")
public class MovieCrewController {

    @Autowired
    private MovieCrewService movieCrewService;

    @ApiPageable
    @ApiOperation(value = "Get all movie crew for movie")
    @GetMapping
    public PageResult<MovieCrewReadDTO> getAllMovieCrews(
            @PathVariable UUID movieId,
            @ApiIgnore Pageable pageable
    ) {
        return movieCrewService.getAllMovieCrews(movieId, pageable);
    }

    @ApiOperation(value = "Get single movie crew for movie")
    @GetMapping("/{id}")
    public MovieCrewReadDTO getMovieCrew(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieCrewService.getMovieCrew(movieId, id);
    }

    @ApiOperation(value = "Get movie crew with details for movie")
    @GetMapping("/{id}/extended")
    public MovieCrewReadExtendedDTO getExtendedMovieCrew(@PathVariable UUID movieId, @PathVariable UUID id) {
        return movieCrewService.getExtendedMovieCrew(movieId, id);
    }

    @ApiOperation(value = "Create movie crew for movie",
            notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @PostMapping
    public MovieCrewReadDTO createMovieCrew(
            @RequestBody @Valid MovieCrewCreateDTO createDTO,
            @PathVariable UUID movieId
    ) {
        return movieCrewService.createMovieCrew(createDTO, movieId);
    }

    @ApiOperation(value = "Update movie crew for movie",
            notes = "Needs CONTENT_MANAGER authority. Empty fields will not be updated")
    @ContentManager
    @PatchMapping("/{id}")
    public MovieCrewReadDTO patchMovieCrew(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid MovieCrewPatchDTO patchDTO
    ) {
        return movieCrewService.patchMovieCrew(movieId, id, patchDTO);
    }

    @ApiOperation(value = "Update movie crew for movie",
            notes = "Needs CONTENT_MANAGER authority. All fields will be updated")
    @ContentManager
    @PutMapping("/{id}")
    public MovieCrewReadDTO updateMovieCrew(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid MovieCrewPutDTO updateDTO
    ) {
        return movieCrewService.updateMovieCrew(movieId, id, updateDTO);
    }

    @ApiOperation(value = "Delete movie crew for movie",
            notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @DeleteMapping("/{id}")
    public void deleteMovieCrew(@PathVariable UUID movieId, @PathVariable UUID id) {
        movieCrewService.deleteMovieCrew(movieId, id);
    }
}
