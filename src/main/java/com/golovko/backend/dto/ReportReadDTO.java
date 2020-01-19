package com.golovko.backend.dto;

import com.golovko.backend.domain.ReportType;
import lombok.Data;

import java.util.UUID;

@Data
public class ReportReadDTO {
    private UUID id;

    private String reportTitle;

    private String reportText;

    private ReportType reportType;
}
