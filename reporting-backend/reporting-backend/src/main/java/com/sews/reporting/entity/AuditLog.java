package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action;

    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    public enum ActionType {
        LOGIN,
        LOGOUT,
        IMPORT_EXCEL,
        GENERATE_REPORT,
        DOWNLOAD_REPORT,
        SEND_EMAIL,
        CREATE_USER,
        UPDATE_USER,
        DELETE_USER,
        CREATE_SCHEDULE,
        UPDATE_SCHEDULE,
        DELETE_SCHEDULE,
        UPDATE_SETTINGS,
        DELETE_DATA,
        UPDATE_PERMISSIONS,
        OTHER
    }
}
