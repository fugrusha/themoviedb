package com.golovko.backend.controller;

import com.golovko.backend.dto.movieparticipation.*;
import com.golovko.backend.service.MovieParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/{movieId}/movie-participations")
public class MovieParticipationController {

    @Autowired
    private MovieParticipationService movieParticipationService;

    @GetMapping
    public List<MoviePartReadDTO> getListOfMovieCast(@PathVariable UUID movieId) {
        return movieParticipationService.getListOfMovieParticipation(movieId);
    }

    @GetMapping("/{id}")
    public MoviePartReadDTO getMovieParticipation(@PathVariable UUID movieId, @PathVariable UUID id){
        return movieParticipationService.getMovieParticipation(movieId, id);
    }

    @GetMapping("/{id}/extended")
    public MoviePartReadExtendedDTO getExtendedMovieParticipation(@PathVariable UUID movieId, @PathVariable UUID id){
        return movieParticipationService.getExtendedMovieParticipation(movieId, id);
    }

    @PostMapping
    public MoviePartReadDTO createMovieParticipation(
            @RequestBody MoviePartCreateDTO createDTO,
            @PathVariable UUID movieId
    ){
        return movieParticipationService.createMovieParticipation(createDTO, movieId);
    }

    @PatchMapping("/{id}")
    public MoviePartReadDTO patchMovieParticipation(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody MoviePartPatchDTO patchDTO
    ){
        return movieParticipationService.patchMovieParticipation(movieId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public MoviePartReadDTO updateMovieParticipation(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody MoviePartPutDTO updateDTO
    ) {
        return movieParticipationService.updateMovieParticipation(movieId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieParticipation(@PathVariable UUID movieId, @PathVariable UUID id) {
        movieParticipationService.deleteMovieParticipation(movieId, id);
    }
}
