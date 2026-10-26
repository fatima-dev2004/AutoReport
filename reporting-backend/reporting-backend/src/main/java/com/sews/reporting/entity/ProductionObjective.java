package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "production_objectives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionObjective {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "active")
    private Boolean active = true; // Objectif actif ou archivé
    
    @Column(nullable = false, name = "end_date")
    private LocalDate endDate;
    
    @Column(nullable = false, name = "line")
    private String line; // Ligne de production (A, B, C, etc.)
    
    @Column(nullable = false, name = "max_defects")
    private Integer maxDefects = 0; // Nombre maximum de défauts acceptés
    
    @Column(name = "notes")
    private String notes; // Notes optionnelles
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "period_type")
    private PeriodType periodType; // DAILY, WEEKLY, MONTHLY
    
    @Column(nullable = false, name = "start_date")
    private LocalDate startDate;
    
    @Column(nullable = false, name = "target_quality_rate")
    private Double targetQualityRate; // Objectif de qualité (ex: 95.0 pour 95%)
    
    @Column(nullable = false, name = "target_quantity")
    private Integer targetQuantity; // Objectif de production
    
    public enum PeriodType {
        DAILY,
        WEEKLY,
        MONTHLY,
        QUARTERLY,
        YEARLY
    }
}
