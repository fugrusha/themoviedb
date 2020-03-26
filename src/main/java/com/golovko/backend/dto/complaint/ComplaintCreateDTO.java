package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class ComplaintCreateDTO {

    @NotNull
    @Size(min = 1, max = 128)
    private String complaintTitle;

    @NotNull
    @Size(min = 1, max = 1000)
    private String complaintText;

    @NotNull
    private ComplaintType complaintType;

    @NotNull
    private TargetObjectType targetObjectType;

    @NotNull
    private UUID targetObjectId;
}
