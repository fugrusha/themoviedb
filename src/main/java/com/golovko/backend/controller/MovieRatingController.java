package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.service.RatingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/movies/{movieId}/ratings")
public class MovieRatingController {

    @Autowired
    private RatingService ratingService;

    @ApiPageable
    @ApiOperation(value = "Get all ratings for movie")
    @GetMapping
    public PageResult<RatingReadDTO> getAllRatingsByMovieId(
            @PathVariable UUID movieId,
            @ApiIgnore Pageable pageable
    ) {
        return ratingService.getRatingsByTargetObjectId(movieId, pageable);
    }

    @ApiOperation(value = "Get single rating for movie")
    @GetMapping("/{id}")
    public RatingReadDTO getMovieRating(@PathVariable UUID movieId, @PathVariable UUID id) {
        return ratingService.getRating(movieId, id);
    }

    @ApiOperation(value = "Create rating for movie")
    @PostMapping
    public RatingReadDTO createMovieRating(
            @PathVariable UUID movieId,
            @RequestBody @Valid RatingCreateDTO createDTO
    ) {
        return ratingService.createRating(movieId, createDTO);
    }

    @ApiOperation(value = "Update rating for movie", notes = "Empty fields will not be updated")
    @PatchMapping("/{id}")
    public RatingReadDTO patchMovieRating(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid RatingPatchDTO patchDTO
    ) {
        return ratingService.patchRating(movieId, id, patchDTO);
    }

    @ApiOperation(value = "Update rating for movie", notes = "All fields will be updated")
    @PutMapping("/{id}")
    public RatingReadDTO updateMovieRating(
            @PathVariable UUID movieId,
            @PathVariable UUID id,
            @RequestBody @Valid RatingPutDTO putDTO
    ) {
        return ratingService.updateRating(movieId, id, putDTO);
    }

    @ApiOperation(value = "Delete rating for movie")
    @DeleteMapping("/{id}")
    public void deleteMovieRating(@PathVariable UUID movieId, @PathVariable UUID id) {
        ratingService.deleteRating(movieId, id);
    }
}
