package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO pour représenter une anomalie détectée dans les données de production
 * Conforme CDC Section 8 : IA pour détection automatique des anomalies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyDetectionDto {
    
    /**
     * Type d'anomalie détectée
     */
    public enum AnomalyType {
        PRODUCTION_DROP,      // Chute de production
        DEFECTS_SPIKE,        // Pic de défauts
        QUALITY_DEGRADATION,  // Dégradation de la qualité
        LINE_STOPPED,         // Ligne arrêtée
        UNUSUAL_PATTERN       // Motif inhabituel
    }
    
    /**
     * Niveau de sévérité de l'anomalie
     */
    public enum Severity {
        LOW,      // Faible
        MEDIUM,   // Moyen
        HIGH,     // Élevé
        CRITICAL  // Critique
    }
    
    private Long id;
    private AnomalyType type;
    private Severity severity;
    private LocalDate detectedDate;
    private String line;  // Ligne de production concernée
    private String description;
    private Double expectedValue;
    private Double actualValue;
    private Double deviation;  // Écart en %
    private Double zScore;     // Score Z statistique
    private String recommendation;
}
