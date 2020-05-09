package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

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

    @Min(value = 0)
    private Integer likesCount;

    @Min(value = 0)
    private Integer dislikesCount;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "article_person",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id"))
    private List<Person> people = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "article_movie",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id"))
    private List<Movie> movies = new ArrayList<>();
}
