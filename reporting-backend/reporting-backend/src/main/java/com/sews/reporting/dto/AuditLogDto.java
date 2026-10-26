package com.sews.reporting.dto;

import com.sews.reporting.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private Long id;
    private Long userId;
    private String userName;
    private String action;
    private AuditLog.ActionType actionType;
    private LocalDateTime timestamp;
    private String details;
    private String ipAddress;
    private String userAgent;
}
