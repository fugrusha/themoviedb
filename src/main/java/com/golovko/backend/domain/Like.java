package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Like extends AbstractEntity {

    private Boolean meLiked;

    @ManyToOne
    @JoinColumn(nullable = false, updatable = false)
    private ApplicationUser author;

    @Enumerated(EnumType.STRING)
    private TargetObjectType likedObjectType;

    @Column(nullable = false)
    private UUID likedObjectId;
}
