package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.UUID;

@Data
public class ComplaintCreateDTO {

    private String complaintTitle;

    private String complaintText;

    private ComplaintType complaintType;

    private TargetObjectType targetObjectType;

    private UUID targetObjectId;
}
