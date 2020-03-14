package com.golovko.backend.dto.misprint;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.UUID;

@Data
public class MisprintCreateDTO {

    private String misprintText;

    private String replaceTo;

    private TargetObjectType targetObjectType;

    private UUID targetObjectId;
}
