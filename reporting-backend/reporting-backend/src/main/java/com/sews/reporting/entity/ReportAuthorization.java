package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Représente une autorisation d'accès à un rapport pour un utilisateur spécifique.
 * Permet de contrôler finement quels rapports un USER peut consulter.
 * 
 * Conforme au CDC Section 5.1 : "L'employé a un accès limité aux rapports autorisés uniquement"
 */
@Entity
@Table(name = "report_authorizations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"report_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportAuthorization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ProductionReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User authorizedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authorized_by_user_id")
    private User authorizedBy;

    private LocalDateTime authorizedAt;

    @Column(length = 500)
    private String authorizationReason;

    @PrePersist
    public void prePersist() {
        this.authorizedAt = LocalDateTime.now();
    }
}
