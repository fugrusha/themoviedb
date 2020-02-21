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
public class Article {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String title;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String text;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    @ManyToOne
    private ApplicationUser author;

    private Integer likesCount;

    private Integer dislikesCount;

    // TODO relatedPersons for articles
    // TODO relatedMovies for articles

}
