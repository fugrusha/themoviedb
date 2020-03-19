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
public class Rating extends AbstractEntity {

    private Integer rating;

    @ManyToOne
    private ApplicationUser author;

    @Enumerated(EnumType.STRING)
    private TargetObjectType ratedObjectType;

    @Column(nullable = false)
    private UUID ratedObjectId;
}
