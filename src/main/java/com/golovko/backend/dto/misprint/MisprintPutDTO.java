package com.golovko.backend.dto.misprint;

import lombok.Data;

@Data
public class MisprintPutDTO {

    private String misprintText;

    private String replaceTo;
}
