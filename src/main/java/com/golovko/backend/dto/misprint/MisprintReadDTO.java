package com.golovko.backend.dto.misprint;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MisprintReadDTO {

    private UUID id;

    private String misprintText;

    private String replaceTo;

    private ComplaintStatus status;

    private UUID authorId;

    private UUID moderatorId;

    private TargetObjectType targetObjectType;

    private UUID targetObjectId;

    private Instant createdAt;

    private Instant updatedAt;

    private Instant fixedAt;

    private String replacedWith;

    private String reason;
}
