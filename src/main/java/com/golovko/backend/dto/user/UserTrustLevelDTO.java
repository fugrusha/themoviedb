package com.golovko.backend.dto.user;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class UserTrustLevelDTO {

    @Min(value = 1)
    @Max(value = 10)
    @NotNull
    private Double trustLevel;
}
