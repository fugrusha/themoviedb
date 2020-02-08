package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import lombok.Data;

@Data
public class ComplaintPutDTO {

    private String complaintTitle;

    private String complaintText;

    private ComplaintType complaintType;

    private ComplaintStatus complaintStatus;
}
