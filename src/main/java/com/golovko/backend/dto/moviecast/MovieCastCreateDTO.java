package com.golovko.backend.dto.moviecast;

import lombok.Data;

import java.util.UUID;

@Data
public class MovieCastCreateDTO {

    private UUID personId;

    private String description;

    private String character;
}
