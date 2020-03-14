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
public class Like {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private Boolean meLiked;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private ApplicationUser author;

    @Enumerated(EnumType.STRING)
    private TargetObjectType likedObjectType;

    @Column(nullable = false)
    private UUID likedObjectId;
}
