package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class MovieParticipation {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String partInfo;

    @ElementCollection(targetClass = PartType.class)
    @CollectionTable(name = "part_type", joinColumns = @JoinColumn(name = "movie_participation_id"))
    @Enumerated(EnumType.STRING)
    private Set<PartType> partTypes = new HashSet<>();

    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person person;

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;
}
