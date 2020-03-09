package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ComplaintFilter {

    private UUID moderatorId;

    private UUID authorId;

    private Set<TargetObjectType> targetObjectTypes;

    private Set<ComplaintType> complaintTypes;

    private Set<ComplaintStatus> statuses;
}
