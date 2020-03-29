package com.golovko.backend.dto.complaint;

import com.golovko.backend.domain.ComplaintType;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ComplaintPutDTO {

    @Size(min = 1, max = 128)
    private String complaintTitle;

    @Size(min = 1, max = 1000)
    private String complaintText;

    private ComplaintType complaintType;
}
