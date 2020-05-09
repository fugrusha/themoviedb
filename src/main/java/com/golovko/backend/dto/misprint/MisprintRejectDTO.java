package com.golovko.backend.dto.misprint;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MisprintRejectDTO {

    @NotNull
    private ComplaintStatus status;

    @NotNull
    @Size(min = 1, max = 150)
    private String reason;

    @NotNull
    private UUID moderatorId;
}
