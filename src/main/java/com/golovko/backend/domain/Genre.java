package com.golovko.backend.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
public class Genre extends AbstractEntity {

    @NotNull
    @Size(min = 1, max = 100)
    private String genreName;

    @Size(min = 1, max = 500)
    private String description;

    @ManyToMany(mappedBy = "genres")
    private List<Movie> movies = new ArrayList<>();
}
