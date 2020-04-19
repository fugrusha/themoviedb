package com.golovko.backend.client.themoviedb.dto;

import lombok.Data;

import java.util.List;

@Data
public class MovieCreditsReadDTO {

    private String id;

    private List<CastReadDTO> cast;

    private List<CrewReadDTO> crew;
}
