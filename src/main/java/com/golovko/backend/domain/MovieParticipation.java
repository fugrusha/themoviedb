package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
public class MovieParticipation {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String partInfo;

    @Enumerated(EnumType.STRING)
    private PartType partType;

    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;
}
