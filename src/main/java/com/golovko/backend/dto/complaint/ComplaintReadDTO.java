package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import lombok.Data;

import java.util.UUID;

@Data
public class ComplaintReadDTO {

    private UUID id;

    private String complaintTitle;

    private String complaintText;

    private ComplaintType complaintType;

    private ComplaintStatus complaintStatus;

    private UUID authorId;
}
