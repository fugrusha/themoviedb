package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Rating extends AbstractEntity {

    private Integer rating;

    @ManyToOne
    private ApplicationUser author;

    @Enumerated(EnumType.STRING)
    private TargetObjectType ratedObjectType;

    @Column(nullable = false)
    private UUID ratedObjectId;
}
