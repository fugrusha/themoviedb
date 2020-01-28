package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
public class MovieCast {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String partInfo;

    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;

    private String character;

    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private PartType partType = PartType.CAST;
}
