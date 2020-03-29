package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class ComplaintModerateDTO {

    @NotNull
    private UUID moderatorId;

    @NotNull
    private ComplaintStatus complaintStatus;
}
