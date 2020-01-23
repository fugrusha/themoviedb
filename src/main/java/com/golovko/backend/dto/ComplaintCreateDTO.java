package com.golovko.backend.dto;

import com.golovko.backend.domain.ComplaintType;
import lombok.Data;

@Data
public class ComplaintCreateDTO {
    private String complaintTitle;

    private String complaintText;

    private ComplaintType complaintType;

//    private UUID authorId;
}
