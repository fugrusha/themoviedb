package com.golovko.backend.dto.moviecast;

import lombok.Data;

import java.util.UUID;

@Data
public class MovieCastPutDTO {

    private String description
            ;

    private String character;

    private UUID personId;
}
