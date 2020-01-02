package com.golovko.backend.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape;

@Entity
@Table(name = "movies")
@Data
public class Movie {

    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    private String movieTitle;

    @JsonFormat(pattern = "dd-MM-yyyy", shape = Shape.STRING)
    private Date releaseDate;
    private String description;
    private boolean isReleased;
    private double averageRating;
}
