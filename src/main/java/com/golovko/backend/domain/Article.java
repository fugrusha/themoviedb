package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Entity
@Getter
@Setter
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
