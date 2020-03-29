package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Getter
@Setter
public class Article extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 300)
    private String title;

    @NotNull
    @Size(min = 1, max = 10000)
    private String text;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    @NotNull
    @ManyToOne
    private ApplicationUser author;

    private Integer likesCount;

    private Integer dislikesCount;

    // TODO relatedPersons for articles
    // TODO relatedMovies for articles
}
