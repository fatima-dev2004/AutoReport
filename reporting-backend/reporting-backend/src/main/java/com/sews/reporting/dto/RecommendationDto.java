package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les recommandations d'amélioration générées par l'IA
 * Conforme CDC Section 8 : IA pour proposer des recommandations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationDto {
    
    /**
     * Catégorie de la recommandation
     */
    public enum Category {
        QUALITY_IMPROVEMENT,    // Amélioration qualité
        PRODUCTION_OPTIMIZATION, // Optimisation production
        MAINTENANCE,            // Maintenance préventive
        TRAINING,               // Formation du personnel
        PROCESS_IMPROVEMENT     // Amélioration des processus
    }
    
    /**
     * Priorité de la recommandation
     */
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
    
    private Long id;
    private Category category;
    private Priority priority;
    private String title;
    private String description;
    private String basedOn;  // Sur quoi se base cette recommandation
    private Double expectedImpact;  // Impact attendu en %
    private Integer estimatedDays;  // Délai estimé pour mise en œuvre
    private String actionable;  // Action concrète recommandée
    private LocalDateTime generatedAt;
}
