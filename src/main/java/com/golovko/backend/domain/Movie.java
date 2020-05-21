package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Movie extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 128)
    private String movieTitle;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Size(min = 1, max = 1000)
    private String description;

    @Size(min = 1, max = 100)
    private String posterUrl;

    @Size(min = 1, max = 100)
    private String trailerUrl;

    @PositiveOrZero
    private Integer runtime;

    @PositiveOrZero
    private Integer revenue;

    @NotNull
    private Boolean isReleased;

    @Min(value = 0)
    @Max(value = 10)
    private Double averageRating;

    @Min(value = 0)
    @Max(value = 10)
    private Double predictedAverageRating;

    @Min(value = 0)
    private Integer likesCount;

    @Min(value = 0)
    private Integer dislikesCount;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieCrew> movieCrews = new ArrayList<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieCast> movieCasts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "genre_movie",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres = new ArrayList<>();

    @ManyToMany(mappedBy = "movies")
    private List<Article> articles = new ArrayList<>();

    @ManyToMany(mappedBy = "movies")
    private List<Watchlist> watchlists = new ArrayList<>();
}
