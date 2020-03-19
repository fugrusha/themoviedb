package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Article extends AbstractEntity {

    private String title;

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
