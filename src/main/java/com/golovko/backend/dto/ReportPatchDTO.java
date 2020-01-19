package com.golovko.backend.dto;

import com.golovko.backend.domain.ReportType;
import lombok.Data;

@Data
public class ReportPatchDTO {
    private String reportTitle;

    private String reportText;

    private ReportType reportType;
}
