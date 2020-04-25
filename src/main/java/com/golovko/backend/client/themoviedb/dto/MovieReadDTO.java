package com.golovko.backend.client.themoviedb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class MovieReadDTO {

    private String id;

    private String originalTitle;

    private String title;

    private String status;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate releaseDate;

    private String overview;

    private Integer runtime;

    private Integer revenue;

    private List<GenreShortDTO> genres;
}
