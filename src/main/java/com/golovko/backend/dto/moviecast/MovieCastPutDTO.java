package com.golovko.backend.dto.moviecast;

import lombok.Data;

import java.util.UUID;

@Data
public class MovieCastPutDTO {

    private String partInfo;

    private String character;

    private UUID personId;
}
