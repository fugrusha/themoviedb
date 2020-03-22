package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Misprint extends AbstractEntity {

    private String misprintText;

    private String replaceTo;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    @ManyToOne
    private ApplicationUser author;

    @ManyToOne
    private ApplicationUser moderator;

    @Enumerated(EnumType.STRING)
    private TargetObjectType targetObjectType;

    @Column(nullable = false)
    private UUID targetObjectId;

    private Instant fixedAt;

    private String replacedWith;

    private String reason;
}

