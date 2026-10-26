package com.sews.reporting.controller;

import com.sews.reporting.dto.AuditLogDto;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_LOGS')")
    public ResponseEntity<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditLog.ActionType actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<AuditLogDto> logs = auditLogService.getAuditLogs(userId, actionType, startDate, endDate, page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAuthority('VIEW_LOGS')")
    public ResponseEntity<List<AuditLogDto>> getRecentLogs(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<AuditLogDto> logs = auditLogService.getRecentLogs(limit);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('VIEW_LOGS')")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(auditLogService.getStatsByCategory());
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('VIEW_LOGS')")
    public ResponseEntity<Resource> exportCsv(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) AuditLog.ActionType actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        byte[] csv = auditLogService.exportToCsv(userId, actionType, startDate, endDate);
        Resource resource = new ByteArrayResource(csv);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-log.csv\"")
                .body(resource);
    }
}
