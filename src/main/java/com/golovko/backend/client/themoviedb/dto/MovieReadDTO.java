package com.golovko.backend.client.themoviedb.dto;

import lombok.Data;

import java.util.List;

@Data
public class MovieReadDTO {

    private String id;

    private String originalTitle;

    private String title;

    private String status;

    private String releaseDate;

    private String overview;

    private List<GenreShortDTO> genres;
}
