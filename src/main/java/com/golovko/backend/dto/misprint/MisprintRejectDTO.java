package com.golovko.backend.dto.misprint;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class MisprintRejectDTO {

    private ComplaintStatus status;

    private String reason;

    private UUID moderatorId;
}
