package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/movie-casts/{movieCastId}/ratings")
public class MovieCastRatingController {

    @Autowired
    private RatingService ratingService;

    @GetMapping
    public PageResult<RatingReadDTO> getRatingsByMovieCastId(
            @PathVariable UUID movieCastId,
            Pageable pageable
    ) {
        return ratingService.getRatingsByTargetObjectId(movieCastId, pageable);
    }

    @GetMapping("/{id}")
    public RatingReadDTO getMovieCastRating(@PathVariable UUID movieCastId, @PathVariable UUID id) {
        return ratingService.getRating(movieCastId, id);
    }

    @PostMapping
    public RatingReadDTO createMovieCastRating(
            @PathVariable UUID movieCastId,
            @RequestBody @Valid RatingCreateDTO createDTO
    ) {
        return ratingService.createRating(movieCastId, createDTO);
    }

    @PatchMapping("/{id}")
    public RatingReadDTO patchMovieCastRating(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id,
            @RequestBody @Valid RatingPatchDTO patchDTO
    ) {
        return ratingService.patchRating(movieCastId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public RatingReadDTO updateMovieCastRating(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id,
            @RequestBody @Valid RatingPutDTO putDTO
    ) {
        return ratingService.updateRating(movieCastId, id, putDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteMovieCastRating(@PathVariable UUID movieCastId, @PathVariable UUID id) {
        ratingService.deleteRating(movieCastId, id);
    }
}
