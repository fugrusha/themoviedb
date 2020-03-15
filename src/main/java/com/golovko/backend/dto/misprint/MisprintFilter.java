package com.golovko.backend.dto.misprint;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class MisprintFilter {

    private UUID moderatorId;

    private UUID authorId;

    private Set<TargetObjectType> targetObjectTypes;

    private Set<ComplaintStatus> statuses;
}
