package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ComplaintModerateDTO {

    private UUID moderatorId;

    private ComplaintStatus complaintStatus;
}
