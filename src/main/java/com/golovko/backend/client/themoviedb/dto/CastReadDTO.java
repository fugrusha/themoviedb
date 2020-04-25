package com.golovko.backend.client.themoviedb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CastReadDTO {

    private String castId;

    private String character;

    @JsonProperty("id")
    private String personId;

    private Integer gender;

    private Integer order;
}
