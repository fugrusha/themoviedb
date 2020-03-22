package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class MovieCrew extends AbstractEntity {

    private String description;

    @Enumerated(EnumType.STRING)
    private MovieCrewType movieCrewType;

    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;
}
