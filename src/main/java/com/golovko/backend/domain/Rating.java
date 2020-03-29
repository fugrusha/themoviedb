package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Rating extends AbstractEntity {

    @NotNull
    @Min(value = 1)
    @Max(value = 10)
    private Integer rating;

    @NotNull
    @ManyToOne
    private ApplicationUser author;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TargetObjectType ratedObjectType;

    @NotNull
    @Column(nullable = false)
    private UUID ratedObjectId;
}
