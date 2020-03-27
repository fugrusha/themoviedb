package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Misprint extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 150)
    private String misprintText;

    @NotNull
    @Size(min = 1, max = 150)
    private String replaceTo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    @NotNull
    @ManyToOne
    private ApplicationUser author;

    @ManyToOne
    private ApplicationUser moderator;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TargetObjectType targetObjectType;

    @NotNull
    private UUID targetObjectId;

    private Instant fixedAt;

    @Size(min = 1, max = 150)
    private String replacedWith;

    @Size(min = 1, max = 150)
    private String reason;
}

