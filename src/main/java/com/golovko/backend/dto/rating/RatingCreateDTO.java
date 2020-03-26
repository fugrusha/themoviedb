package com.golovko.backend.dto.rating;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class RatingCreateDTO {

    @Min(value = 1)
    @Max(value = 10)
    @NotNull
    private Integer rating;

    @NotNull
    private UUID authorId;

    @NotNull
    private TargetObjectType ratedObjectType;
}
