package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
public class Movie {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Column(nullable = false)
    private String movieTitle;

    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;

    private Double averageRating;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private Set<MovieParticipation> movieParticipations = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCast> movieCast = new HashSet<>();

    // TODO genres for movies
//    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<Genre> genres = new HashSet<>();
    // TODO relatedArticles for movies
//    @OneToMany(mappedBy = "")
//    private List<Article> relatedArticles = new ArrayList<>();
}
