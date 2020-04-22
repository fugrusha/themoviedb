package com.golovko.backend.dto.movie;

import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewReadDTO;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class MovieReadExtendedDTO {

    private UUID id;

    private String movieTitle;

    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;

    private String posterUrl;

    private String trailerUrl;

    private Integer runtime;

    private Integer revenue;

    private Double averageRating;

    private Double predictedAverageRating;

    private Integer likesCount;

    private Integer dislikesCount;

    private Instant createdAt;

    private Instant updatedAt;

    private List<GenreReadDTO> genres;

    private List<MovieCrewReadDTO> movieCrews;

    private List<MovieCastReadDTO> movieCasts;
}
