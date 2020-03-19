package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class MovieCast extends AbstractEntity {

    private String description;

    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;

    private String character;

    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private MovieCrewType movieCrewType = MovieCrewType.CAST;
}
