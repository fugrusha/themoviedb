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
@RequestMapping("/api/v1/movies/{movieId}/movie-crews/{movieCrewId}/ratings")
public class MovieCrewRatingController {

    @Autowired
    private RatingService ratingService;

    @ApiPageable
    @ApiOperation(value = "Get all ratings for movie crew")
    @GetMapping
    public PageResult<RatingReadDTO> getRatingsByMovieCrewId(
            @PathVariable UUID movieCrewId,
            @ApiIgnore Pageable pageable
    ) {
        return ratingService.getRatingsByTargetObjectId(movieCrewId, pageable);
    }

    @ApiOperation(value = "Get single rating for movie crew")
    @GetMapping("/{id}")
    public RatingReadDTO getMovieCrewRating(@PathVariable UUID movieCrewId, @PathVariable UUID id) {
        return ratingService.getRating(movieCrewId, id);
    }

    @ApiOperation(value = "Create rating for movie crew")
    @PostMapping
    public RatingReadDTO createMovieCrewRating(
            @PathVariable UUID movieCrewId,
            @RequestBody @Valid RatingCreateDTO createDTO
    ) {
        return ratingService.createRating(movieCrewId, createDTO);
    }

    @ApiOperation(value = "Update rating for movie crew",
            notes = "Empty fields will not be updated")
    @PatchMapping("/{id}")
    public RatingReadDTO patchMovieCrewRating(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody @Valid RatingPatchDTO patchDTO
    ) {
        return ratingService.patchRating(movieCrewId, id, patchDTO);
    }

    @ApiOperation(value = "Update rating for movie crew",
            notes = "All fields will be updated")
    @PutMapping("/{id}")
    public RatingReadDTO updateMovieCrewRating(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id,
            @RequestBody @Valid RatingPutDTO putDTO
    ) {
        return ratingService.updateRating(movieCrewId, id, putDTO);
    }

    @ApiOperation(value = "Delete rating for movie crew")
    @DeleteMapping("/{id}")
    public void deleteMovieCrewRating(@PathVariable UUID movieCrewId, @PathVariable UUID id) {
        ratingService.deleteRating(movieCrewId, id);
    }
}
