package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Person extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 128)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 128)
    private String lastName;

    @NotNull
    @Size(min = 1, max = 1000)
    private String bio;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Min(value = 0)
    @Max(value = 10)
    private Double averageRatingByRoles;

    @Min(value = 0)
    @Max(value = 10)
    private Double averageRatingByMovies;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCrew> movieCrews = new HashSet<>();

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCast> movieCast = new HashSet<>();

    @ManyToMany(mappedBy = "people")
    private List<Article> articles = new ArrayList<>();
}
