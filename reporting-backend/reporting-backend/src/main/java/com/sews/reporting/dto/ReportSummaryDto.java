package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ReportSummaryDto {
    private Long id;
    private String title;
    private String type;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Double totalProduction;
    private Integer totalDefects;
    private Double qualityRate;
    private String generatedByEmail;
    private LocalDateTime generatedAt;
}
