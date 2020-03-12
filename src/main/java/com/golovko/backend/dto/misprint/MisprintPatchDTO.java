package com.golovko.backend.dto.misprint;

import lombok.Data;

@Data
public class MisprintPatchDTO {

    private Integer startIndex;

    private Integer endIndex;

    private String replaceTo;
}
