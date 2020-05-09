package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "like_entity")
public class Like extends AbstractEntity {

    @NotNull
    private Boolean meLiked;

    @NotNull
    @ManyToOne
    @JoinColumn(updatable = false)
    private ApplicationUser author;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TargetObjectType likedObjectType;

    @NotNull
    private UUID likedObjectId;
}
