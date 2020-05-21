package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Watchlist extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 128)
    private String name;

    @NotNull
    @ManyToOne
    private ApplicationUser author;

    @ManyToMany
    @JoinTable(
            name = "movie_watchlist",
            joinColumns = @JoinColumn(name = "watchlist_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    private List<Movie> movies = new ArrayList<>();
}
