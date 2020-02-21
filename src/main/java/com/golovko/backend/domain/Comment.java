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
public class Comment {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String message;

    @Enumerated(value = EnumType.STRING)
    private CommentStatus status;

    private Integer likesCount;

    private Integer dislikesCount;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @ManyToOne
    private ApplicationUser author;

    @Enumerated(EnumType.STRING)
    private ParentType parentType;

    @Column(nullable = false)
    private UUID parentId;
}
