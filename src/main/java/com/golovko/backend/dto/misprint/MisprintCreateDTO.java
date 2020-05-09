package com.golovko.backend.dto.misprint;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MisprintCreateDTO {

    @NotNull
    @Size(min = 1, max = 150)
    private String misprintText;

    @Size(min = 1, max = 150)
    private String replaceTo;

    @NotNull
    private TargetObjectType targetObjectType;

    @NotNull
    private UUID targetObjectId;
}
