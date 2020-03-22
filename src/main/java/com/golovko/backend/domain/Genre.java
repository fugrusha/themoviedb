package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
public class Genre extends AbstractEntity {

    @Column(nullable = false)
    private String genreName;

    private String description;

    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies = new HashSet<Movie>();
}
