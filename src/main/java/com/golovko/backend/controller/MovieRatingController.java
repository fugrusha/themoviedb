package com.golovko.backend.controller;

import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/ratings")
public class MovieRatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping
    public List<RatingReadDTO> getAllRatingsByMovieId(@PathVariable UUID movieId) {
        return ratingService.getAllRatingsByMovieId(movieId);
    }

    @GetMapping("/{id}")
    public RatingReadDTO getMovieRating(@PathVariable UUID movieId, @PathVariable UUID id) {
        return ratingService.getRating(movieId, id);
    }

    @PostMapping
    public RatingReadDTO createMovieRating(
            @PathVariable UUID movieId,
            @RequestBody RatingCreateDTO createDTO
    ) {
        return ratingService.createRating(movieId, createDTO);
    }

    @PatchMapping("/{id}")
    public RatingReadDTO patchMovieRating(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody RatingPatchDTO patchDTO
    ) {
        return ratingService.patchRating(movieId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public RatingReadDTO updateMovieRating(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody RatingPutDTO putDTO
    ) {
        return ratingService.updateRating(movieId, id, putDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieRating(@PathVariable UUID movieId, @PathVariable UUID id) {
        ratingService.deleteRating(movieId, id);
    }
}
