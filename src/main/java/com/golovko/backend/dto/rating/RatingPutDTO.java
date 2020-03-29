package com.golovko.backend.dto.rating;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class RatingPutDTO {

    @Min(value = 1)
    @Max(value = 10)
    private Integer rating;
}
