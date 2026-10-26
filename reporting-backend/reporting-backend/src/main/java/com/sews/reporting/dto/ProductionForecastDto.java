package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO pour les prédictions de production
 * Conforme CDC Section 8 : IA pour prédire les tendances
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionForecastDto {
    
    /**
     * Un point de prédiction individuel
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ForecastPoint {
        private LocalDate date;
        private Double predictedProduction;
        private Double predictedDefects;
        private Double predictedQualityRate;
        private Double confidenceLevel;  // Niveau de confiance (0-100%)
    }
    
    private LocalDate generatedAt;
    private String method;  // Méthode utilisée (MOVING_AVERAGE, LINEAR_REGRESSION, etc.)
    private Integer daysAhead;
    private List<ForecastPoint> predictions;
    private Double averageHistoricalProduction;
    private String trend;  // INCREASING, DECREASING, STABLE
    private String insight;  // Résumé textuel de la prédiction
}
