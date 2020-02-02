package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Article {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String title;

    private Instant publishedDate;

    private String text;

    @Enumerated(EnumType.STRING)
    private ArticleStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "author_id")
    private ApplicationUser author;

    // TODO comments for articles
//    private List<Comment> comments;
    // TODO relatedPersons for articles
//    private List<Person> relatedPeople;
    // TODO relatedMovies for articles
//    private List<Movie> relatedMovies;

    private Integer likesCount;

    private Integer dislikesCount;
}
