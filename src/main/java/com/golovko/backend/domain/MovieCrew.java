package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
public class MovieCrew extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 1000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MovieCrewType movieCrewType;

    @Min(value = 0)
    @Max(value = 10)
    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;
}
