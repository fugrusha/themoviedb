package com.golovko.backend.client.themoviedb.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CrewReadDTO {

    private String creditId;

    private String department;

    private String job;

    @JsonProperty("id")
    private String personId;

}
