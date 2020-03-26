package com.golovko.backend.dto.moviecast;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MovieCastCreateDTO {

    private UUID personId;

    @NotNull
    @Size(min = 1, max = 1000)
    private String description;

    @NotNull
    @Size(min = 1, max = 50)
    private String character;
}
