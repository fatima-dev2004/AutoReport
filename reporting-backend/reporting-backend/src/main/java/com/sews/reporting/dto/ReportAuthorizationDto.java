package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportAuthorizationDto {
    private Long id;
    private Long reportId;
    private String reportTitle;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long authorizedByUserId;
    private String authorizedByUserName;
    private LocalDateTime authorizedAt;
    private String authorizationReason;
}
