package com.golovko.backend.controller;

import com.golovko.backend.dto.movieparticipation.*;
import com.golovko.backend.service.MovieParticipationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movie-participations")
public class MovieParticipationController {

    @Autowired
    private MovieParticipationService movieParticipationService;

    @GetMapping("/{id}")
    public MoviePartReadDTO getMovieParticipation(@PathVariable UUID id){
        return movieParticipationService.getMovieParticipation(id);
    }

    @GetMapping("/{id}/extended")
    public MoviePartReadExtendedDTO getExtendedMovieParticipation(@PathVariable UUID id){
        return movieParticipationService.getExtendedMovieParticipation(id);
    }

    @PostMapping
    public MoviePartReadDTO createMovieParticipation(
            @RequestBody MoviePartCreateDTO createDTO,
            @RequestParam("movieId") UUID movieId,
            @RequestParam("personId") UUID personId
    ){
        return movieParticipationService.createMovieParticipation(createDTO, movieId, personId);
    }

    @PatchMapping("/{id}")
    public MoviePartReadDTO patchMovieParticipation(
            @PathVariable UUID id, @RequestBody MoviePartPatchDTO patchDTO
    ){
        return movieParticipationService.patchMovieParticipation(id, patchDTO);
    }

    @PutMapping("/id")
    public MoviePartReadDTO updateMovieParticipation(
            @PathVariable UUID id, @RequestBody MoviePartPutDTO updateDTO
    ) {
        return movieParticipationService.updateMovieParticipation(id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieParticipation(@PathVariable UUID id) {
        movieParticipationService.deleteMovieParticipation(id);
    }
}
