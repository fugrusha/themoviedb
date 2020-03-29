package com.golovko.backend.dto.genre;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class GenrePatchDTO {

    @Size(min = 1, max = 100)
    private String genreName;

    @Size(min = 1, max = 500)
    private String description;
}
