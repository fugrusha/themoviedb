package com.golovko.backend.dto.misprint;

import lombok.Data;

@Data
public class MisprintPatchDTO {

    private String misprintText;

    private String replaceTo;
}
