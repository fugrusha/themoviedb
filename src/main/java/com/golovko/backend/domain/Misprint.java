package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Misprint {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

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

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

