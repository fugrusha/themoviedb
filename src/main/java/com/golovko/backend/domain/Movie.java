package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @NotNull
    private Boolean isReleased;

    @Min(value = 0)
    @Max(value = 10)
    private Double averageRating;

    private Integer likesCount;

    private Integer dislikesCount;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCrew> movieCrews = new HashSet<MovieCrew>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCast> movieCasts = new HashSet<MovieCast>();

    @ManyToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(
            name = "genre_movie",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<Genre>();
}
