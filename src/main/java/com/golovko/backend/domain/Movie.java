package com.golovko.backend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape;

@Entity
@Setter
@Getter
public class Movie {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String movieTitle;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = Shape.STRING)
    private LocalDate releaseDate;

    private String description;

    private boolean isReleased;

    private Double averageRating;
}
