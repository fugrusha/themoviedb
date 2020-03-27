package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Comment extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 500)
    private String message;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private CommentStatus status;

    private Integer likesCount;

    private Integer dislikesCount;

    @NotNull
    @ManyToOne
    private ApplicationUser author;

    @NotNull
    @Enumerated(EnumType.STRING)
    private TargetObjectType targetObjectType;

    @NotNull
    private UUID targetObjectId;
}
