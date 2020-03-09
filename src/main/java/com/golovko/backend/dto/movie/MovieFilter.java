package com.golovko.backend.dto.movie;

import com.golovko.backend.domain.MovieCrewType;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class MovieFilter {

    private UUID personId;

    private Set<String> genreNames;

    private Set<MovieCrewType> movieCrewTypes;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releasedFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate releasedTo;
}
