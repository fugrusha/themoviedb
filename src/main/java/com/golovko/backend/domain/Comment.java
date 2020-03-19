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
public class Comment extends AbstractEntity {

    private String message;

    @Enumerated(value = EnumType.STRING)
    private CommentStatus status;

    private Integer likesCount;

    private Integer dislikesCount;

    @ManyToOne
    private ApplicationUser author;

    @Enumerated(EnumType.STRING)
    private TargetObjectType targetObjectType;

    @Column(nullable = false)
    private UUID targetObjectId;
}
