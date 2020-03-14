package com.golovko.backend.dto.misprint;

import lombok.Data;

import java.util.UUID;

@Data
public class MisprintConfirmDTO {

    private Integer startIndex;

    private Integer endIndex;

    private String replaceTo;

    private UUID moderatorId;

    private UUID targetObjectId;
}
