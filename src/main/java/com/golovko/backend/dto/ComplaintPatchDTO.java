package com.golovko.backend.dto;

import com.golovko.backend.domain.ComplaintType;
import lombok.Data;

import java.util.UUID;

@Data
public class ComplaintPatchDTO {
    private String complaintTitle;

    private String complaintText;

    private ComplaintType complaintType;

    private UUID authorId;
}
