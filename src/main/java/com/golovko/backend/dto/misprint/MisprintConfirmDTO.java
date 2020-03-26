package com.golovko.backend.dto.misprint;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MisprintConfirmDTO {

    @NotNull
    @PositiveOrZero
    private Integer startIndex;

    @NotNull
    @PositiveOrZero
    private Integer endIndex;

    @NotNull
    @Size(min = 1, max = 150)
    private String replaceTo;

    @NotNull
    private UUID moderatorId;

    @NotNull
    private UUID targetObjectId;
}
