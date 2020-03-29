package com.golovko.backend.dto.moviecast;

import lombok.Data;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MovieCastPatchDTO {

    @Size(min = 1, max = 1000)
    private String description;

    @Size(min = 1, max = 50)
    private String character;

    private UUID personId;
}
