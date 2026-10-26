package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration de planification des rapports par l'administrateur (section 5.8).
 * Exemple : Rapport production -> chaque jour a 08:00 ; Rapport qualite -> chaque semaine.
 */
@Entity
@Table(name = "report_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ProductionReport.ReportType reportType;

    // Expression cron, ex : "0 0 8 * * *" = tous les jours a 08:00
    private String cronExpression;

    // Liste des emails destinataires separes par des virgules
    @Column(length = 1000)
    private String recipients;

    private boolean active = true;
}
