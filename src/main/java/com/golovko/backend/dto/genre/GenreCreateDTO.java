package com.golovko.backend.dto.genre;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class GenreCreateDTO {

    @NotNull
    @Size(min = 1, max = 100)
    private String genreName;

    @NotNull
    @Size(min = 1, max = 500)
    private String description;
}
