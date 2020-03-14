package com.golovko.backend.dto.moderator;

import com.golovko.backend.domain.ComplaintStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class ModeratorDTO {

    private UUID moderatorId;

    private ComplaintStatus complaintStatus;
}
