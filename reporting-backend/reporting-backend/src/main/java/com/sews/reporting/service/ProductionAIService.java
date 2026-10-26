package com.sews.reporting.service;

import com.sews.reporting.dto.AnomalyDetectionDto;
import com.sews.reporting.dto.ProductionForecastDto;
import com.sews.reporting.dto.RecommendationDto;
import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.repository.ProductionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service d'Intelligence Artificielle pour l'analyse prédictive des données de production
 * 
 * Implémente le CDC Section 8 - Évolutions possibles :
 * - Détection automatique des anomalies (algorithme Z-score)
 * - Prédiction des tendances (moyenne mobile)
 * - Génération de recommandations d'amélioration
 * 
 * @author SEWS Reporting System
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionAIService {

    private final ProductionRecordRepository productionRecordRepository;
    
    // Seuils de détection
    private static final double Z_SCORE_THRESHOLD = 2.0;  // 2 écarts-types = anomalie
    private static final double CRITICAL_Z_SCORE = 3.0;   // 3 écarts-types = critique
    
    /**
     * Détecte les anomalies dans les données de production sur une période donnée
     * Utilise l'algorithme Z-score pour identifier les valeurs aberrantes
     * 
     * @param start Date de début
     * @param end Date de fin
     * @return Liste des anomalies détectées
     */
    public List<AnomalyDetectionDto> detectAnomalies(LocalDate start, LocalDate end) {
        log.info("Détection des anomalies entre {} et {}", start, end);
        
        List<ProductionRecord> records = productionRecordRepository.findByProductionDateBetween(start, end);
        
        if (records.isEmpty()) {
            log.warn("Aucune donnée disponible pour la détection d'anomalies");
            return Collections.emptyList();
        }
        
        List<AnomalyDetectionDto> anomalies = new ArrayList<>();
        
        // Grouper par ligne de production
        Map<String, List<ProductionRecord>> byLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine));
        
        // Analyser chaque ligne
        for (Map.Entry<String, List<ProductionRecord>> entry : byLine.entrySet()) {
            String line = entry.getKey();
            List<ProductionRecord> lineRecords = entry.getValue();
            
            // Détecter anomalies de production
            anomalies.addAll(detectProductionAnomalies(line, lineRecords));
            
            // Détecter anomalies de défauts
            anomalies.addAll(detectDefectAnomalies(line, lineRecords));
            
            // Détecter anomalies de qualité
            anomalies.addAll(detectQualityAnomalies(line, lineRecords));
        }
        
        // Trier par sévérité (CRITICAL > HIGH > MEDIUM > LOW)
        anomalies.sort((a, b) -> {
            int severityCompare = b.getSeverity().compareTo(a.getSeverity());
            if (severityCompare != 0) return severityCompare;
            return b.getDetectedDate().compareTo(a.getDetectedDate());
        });
        
        log.info("Détection terminée : {} anomalies trouvées", anomalies.size());
        return anomalies;
    }
    
    /**
     * Détecte les anomalies de production (chutes inhabituelles)
     */
    private List<AnomalyDetectionDto> detectProductionAnomalies(String line, List<ProductionRecord> records) {
        List<AnomalyDetectionDto> anomalies = new ArrayList<>();
        
        // Calculer moyenne et écart-type
        double[] productions = records.stream().mapToDouble(ProductionRecord::getQuantity).toArray();
        double mean = calculateMean(productions);
        double stdDev = calculateStdDev(productions, mean);
        
        if (stdDev == 0) return anomalies;  // Pas de variation
        
        // Détecter les valeurs anormalement basses
        for (ProductionRecord record : records) {
            double zScore = (record.getQuantity() - mean) / stdDev;
            
            if (zScore < -Z_SCORE_THRESHOLD) {  // Production anormalement basse
                AnomalyDetectionDto.Severity severity = zScore < -CRITICAL_Z_SCORE 
                    ? AnomalyDetectionDto.Severity.CRITICAL
                    : (zScore < -2.5 ? AnomalyDetectionDto.Severity.HIGH : AnomalyDetectionDto.Severity.MEDIUM);
                
                double deviation = ((mean - record.getQuantity()) / mean) * 100;
                
                anomalies.add(AnomalyDetectionDto.builder()
                        .type(AnomalyDetectionDto.AnomalyType.PRODUCTION_DROP)
                        .severity(severity)
                        .detectedDate(record.getProductionDate())
                        .line(line)
                        .description(String.format("Chute de production détectée sur %s : %.0f unités (attendu: %.0f)", 
                                line, record.getQuantity(), mean))
                        .expectedValue(mean)
                        .actualValue(record.getQuantity())
                        .deviation(deviation)
                        .zScore(Math.abs(zScore))
                        .recommendation(generateProductionDropRecommendation(deviation))
                        .build());
            }
        }
        
        return anomalies;
    }
    
    /**
     * Détecte les anomalies de défauts (pics inhabituels)
     */
    private List<AnomalyDetectionDto> detectDefectAnomalies(String line, List<ProductionRecord> records) {
        List<AnomalyDetectionDto> anomalies = new ArrayList<>();
        
        double[] defects = records.stream().mapToDouble(ProductionRecord::getDefects).toArray();
        double mean = calculateMean(defects);
        double stdDev = calculateStdDev(defects, mean);
        
        if (stdDev == 0) return anomalies;
        
        for (ProductionRecord record : records) {
            double zScore = (record.getDefects() - mean) / stdDev;
            
            if (zScore > Z_SCORE_THRESHOLD) {  // Défauts anormalement élevés
                AnomalyDetectionDto.Severity severity = zScore > CRITICAL_Z_SCORE 
                    ? AnomalyDetectionDto.Severity.CRITICAL
                    : (zScore > 2.5 ? AnomalyDetectionDto.Severity.HIGH : AnomalyDetectionDto.Severity.MEDIUM);
                
                double deviation = ((record.getDefects() - mean) / mean) * 100;
                
                anomalies.add(AnomalyDetectionDto.builder()
                        .type(AnomalyDetectionDto.AnomalyType.DEFECTS_SPIKE)
                        .severity(severity)
                        .detectedDate(record.getProductionDate())
                        .line(line)
                        .description(String.format("Pic de défauts détecté sur %s : %d défauts (attendu: %.0f)", 
                                line, record.getDefects(), mean))
                        .expectedValue(mean)
                        .actualValue((double) record.getDefects())
                        .deviation(deviation)
                        .zScore(zScore)
                        .recommendation(generateDefectSpikeRecommendation(deviation))
                        .build());
            }
        }
        
        return anomalies;
    }
    
    /**
     * Détecte les dégradations de qualité
     */
    private List<AnomalyDetectionDto> detectQualityAnomalies(String line, List<ProductionRecord> records) {
        List<AnomalyDetectionDto> anomalies = new ArrayList<>();
        
        // Calculer les taux de qualité
        List<Double> qualityRates = records.stream()
                .filter(r -> r.getQuantity() > 0)
                .map(r -> ((r.getQuantity() - r.getDefects()) / r.getQuantity()) * 100)
                .collect(Collectors.toList());
        
        if (qualityRates.size() < 3) return anomalies;  // Pas assez de données
        
        double[] rates = qualityRates.stream().mapToDouble(Double::doubleValue).toArray();
        double mean = calculateMean(rates);
        double stdDev = calculateStdDev(rates, mean);
        
        if (stdDev == 0) return anomalies;
        
        for (int i = 0; i < records.size(); i++) {
            ProductionRecord record = records.get(i);
            if (record.getQuantity() == 0) continue;
            
            double qualityRate = ((record.getQuantity() - record.getDefects()) / record.getQuantity()) * 100;
            double zScore = (qualityRate - mean) / stdDev;
            
            if (zScore < -Z_SCORE_THRESHOLD) {  // Qualité anormalement basse
                AnomalyDetectionDto.Severity severity = zScore < -CRITICAL_Z_SCORE 
                    ? AnomalyDetectionDto.Severity.HIGH
                    : AnomalyDetectionDto.Severity.MEDIUM;
                
                anomalies.add(AnomalyDetectionDto.builder()
                        .type(AnomalyDetectionDto.AnomalyType.QUALITY_DEGRADATION)
                        .severity(severity)
                        .detectedDate(record.getProductionDate())
                        .line(line)
                        .description(String.format("Dégradation de qualité sur %s : %.1f%% (attendu: %.1f%%)", 
                                line, qualityRate, mean))
                        .expectedValue(mean)
                        .actualValue(qualityRate)
                        .deviation(mean - qualityRate)
                        .zScore(Math.abs(zScore))
                        .recommendation("Inspection de la ligne et vérification des procédures qualité recommandée")
                        .build());
            }
        }
        
        return anomalies;
    }
    
    /**
     * Prédit la production future basée sur l'historique
     * Utilise une moyenne mobile simple
     * 
     * @param daysAhead Nombre de jours à prédire
     * @return Prédictions de production
     */
    public ProductionForecastDto predictProduction(int daysAhead) {
        log.info("Prédiction de production pour {} jours", daysAhead);
        
        // Récupérer l'historique des 30 derniers jours
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        
        List<ProductionRecord> historicalRecords = productionRecordRepository.findByProductionDateBetween(start, end);
        
        if (historicalRecords.isEmpty()) {
            log.warn("Pas de données historiques pour la prédiction");
            return ProductionForecastDto.builder()
                    .generatedAt(LocalDate.now())
                    .method("INSUFFICIENT_DATA")
                    .daysAhead(daysAhead)
                    .predictions(Collections.emptyList())
                    .insight("Données insuffisantes pour générer une prédiction")
                    .build();
        }
        
        // Calculer les moyennes
        double avgProduction = historicalRecords.stream()
                .mapToDouble(ProductionRecord::getQuantity)
                .average()
                .orElse(0);
        
        double avgDefects = historicalRecords.stream()
                .mapToDouble(ProductionRecord::getDefects)
                .average()
                .orElse(0);
        
        double avgQualityRate = avgProduction > 0 
                ? ((avgProduction - avgDefects) / avgProduction) * 100 
                : 0;
        
        // Détecter la tendance (augmentation, diminution, stable)
        String trend = detectTrend(historicalRecords);
        double trendFactor = getTrendFactor(trend);
        
        // Générer les prédictions
        List<ProductionForecastDto.ForecastPoint> predictions = new ArrayList<>();
        LocalDate predictionDate = end.plusDays(1);
        
        for (int i = 0; i < daysAhead; i++) {
            // Appliquer le facteur de tendance
            double predictedProduction = avgProduction * (1 + (trendFactor * i / 100));
            double predictedDefects = avgDefects * (1 + (trendFactor * i / 100));
            double predictedQuality = predictedProduction > 0 
                    ? ((predictedProduction - predictedDefects) / predictedProduction) * 100 
                    : avgQualityRate;
            
            // Niveau de confiance décroissant avec la distance
            double confidence = Math.max(50, 95 - (i * 2));
            
            predictions.add(ProductionForecastDto.ForecastPoint.builder()
                    .date(predictionDate.plusDays(i))
                    .predictedProduction(Math.round(predictedProduction * 100.0) / 100.0)
                    .predictedDefects(Math.round(predictedDefects * 100.0) / 100.0)
                    .predictedQualityRate(Math.round(predictedQuality * 100.0) / 100.0)
                    .confidenceLevel(confidence)
                    .build());
        }
        
        String insight = generateForecastInsight(trend, avgProduction, avgQualityRate);
        
        return ProductionForecastDto.builder()
                .generatedAt(LocalDate.now())
                .method("MOVING_AVERAGE_WITH_TREND")
                .daysAhead(daysAhead)
                .predictions(predictions)
                .averageHistoricalProduction(avgProduction)
                .trend(trend)
                .insight(insight)
                .build();
    }
    
    /**
     * Génère des recommandations d'amélioration basées sur l'analyse des données
     * 
     * @return Liste de recommandations
     */
    public List<RecommendationDto> generateRecommendations() {
        log.info("Génération de recommandations d'amélioration");
        
        List<RecommendationDto> recommendations = new ArrayList<>();
        
        // Analyser les 30 derniers jours
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(30);
        List<ProductionRecord> records = productionRecordRepository.findByProductionDateBetween(start, end);
        
        if (records.isEmpty()) {
            return recommendations;
        }
        
        // Calculer les KPI globaux
        double totalProduction = records.stream().mapToDouble(ProductionRecord::getQuantity).sum();
        int totalDefects = records.stream().mapToInt(ProductionRecord::getDefects).sum();
        double qualityRate = totalProduction > 0 
                ? ((totalProduction - totalDefects) / totalProduction) * 100 
                : 0;
        double defectRate = totalProduction > 0 
                ? (totalDefects / totalProduction) * 100 
                : 0;
        
        // Recommandation 1 : Si taux de qualité < 95%
        if (qualityRate < 95.0) {
            recommendations.add(RecommendationDto.builder()
                    .category(RecommendationDto.Category.QUALITY_IMPROVEMENT)
                    .priority(qualityRate < 90 ? RecommendationDto.Priority.HIGH : RecommendationDto.Priority.MEDIUM)
                    .title("Amélioration du taux de qualité")
                    .description(String.format("Le taux de qualité actuel est de %.1f%%, en dessous de l'objectif de 95%%", qualityRate))
                    .basedOn("Analyse des 30 derniers jours de production")
                    .expectedImpact(95.0 - qualityRate)
                    .estimatedDays(14)
                    .actionable("Mettre en place des contrôles qualité supplémentaires et former le personnel aux bonnes pratiques")
                    .generatedAt(java.time.LocalDateTime.now())
                    .build());
        }
        
        // Recommandation 2 : Si taux de défauts > 5%
        if (defectRate > 5.0) {
            recommendations.add(RecommendationDto.builder()
                    .category(RecommendationDto.Category.PROCESS_IMPROVEMENT)
                    .priority(defectRate > 10 ? RecommendationDto.Priority.URGENT : RecommendationDto.Priority.HIGH)
                    .title("Réduction du taux de défauts")
                    .description(String.format("Le taux de défauts est élevé : %.1f%% (objectif < 5%%)", defectRate))
                    .basedOn("Taux de défauts sur 30 jours")
                    .expectedImpact(defectRate - 5.0)
                    .estimatedDays(21)
                    .actionable("Analyser les causes racines des défauts et mettre en place des actions correctives")
                    .generatedAt(java.time.LocalDateTime.now())
                    .build());
        }
        
        // Recommandation 3 : Analyse par ligne
        Map<String, List<ProductionRecord>> byLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine));
        
        String worstLine = null;
        double worstQuality = 100.0;
        
        for (Map.Entry<String, List<ProductionRecord>> entry : byLine.entrySet()) {
            List<ProductionRecord> lineRecords = entry.getValue();
            double lineProduction = lineRecords.stream().mapToDouble(ProductionRecord::getQuantity).sum();
            int lineDefects = lineRecords.stream().mapToInt(ProductionRecord::getDefects).sum();
            double lineQuality = lineProduction > 0 ? ((lineProduction - lineDefects) / lineProduction) * 100 : 0;
            
            if (lineQuality < worstQuality) {
                worstQuality = lineQuality;
                worstLine = entry.getKey();
            }
        }
        
        if (worstLine != null && worstQuality < 92.0) {
            recommendations.add(RecommendationDto.builder()
                    .category(RecommendationDto.Category.MAINTENANCE)
                    .priority(RecommendationDto.Priority.HIGH)
                    .title("Maintenance prioritaire sur " + worstLine)
                    .description(String.format("La ligne %s présente le taux de qualité le plus bas : %.1f%%", worstLine, worstQuality))
                    .basedOn("Comparaison des performances par ligne")
                    .expectedImpact(5.0)
                    .estimatedDays(7)
                    .actionable("Programmer une maintenance préventive et un audit de la ligne " + worstLine)
                    .generatedAt(java.time.LocalDateTime.now())
                    .build());
        }
        
        // Recommandation 4 : Formation si anomalies fréquentes
        List<AnomalyDetectionDto> recentAnomalies = detectAnomalies(start, end);
        long highSeverityAnomalies = recentAnomalies.stream()
                .filter(a -> a.getSeverity() == AnomalyDetectionDto.Severity.HIGH || 
                            a.getSeverity() == AnomalyDetectionDto.Severity.CRITICAL)
                .count();
        
        if (highSeverityAnomalies > 5) {
            recommendations.add(RecommendationDto.builder()
                    .category(RecommendationDto.Category.TRAINING)
                    .priority(RecommendationDto.Priority.MEDIUM)
                    .title("Programme de formation renforcé")
                    .description(String.format("%d anomalies critiques détectées ce mois-ci", highSeverityAnomalies))
                    .basedOn("Analyse des anomalies")
                    .expectedImpact(10.0)
                    .estimatedDays(30)
                    .actionable("Organiser des sessions de formation sur les procédures de production et contrôle qualité")
                    .generatedAt(java.time.LocalDateTime.now())
                    .build());
        }
        
        // Trier par priorité
        recommendations.sort((a, b) -> b.getPriority().compareTo(a.getPriority()));
        
        log.info("{} recommandations générées", recommendations.size());
        return recommendations;
    }
    
    // ===== Méthodes utilitaires statistiques =====
    
    private double calculateMean(double[] values) {
        return Arrays.stream(values).average().orElse(0.0);
    }
    
    private double calculateStdDev(double[] values, double mean) {
        double variance = Arrays.stream(values)
                .map(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
    
    private String detectTrend(List<ProductionRecord> records) {
        if (records.size() < 7) return "STABLE";
        
        // Comparer première et dernière semaine
        int weekSize = Math.min(7, records.size() / 2);
        double firstWeekAvg = records.subList(0, weekSize).stream()
                .mapToDouble(ProductionRecord::getQuantity)
                .average()
                .orElse(0);
        
        double lastWeekAvg = records.subList(records.size() - weekSize, records.size()).stream()
                .mapToDouble(ProductionRecord::getQuantity)
                .average()
                .orElse(0);
        
        double change = ((lastWeekAvg - firstWeekAvg) / firstWeekAvg) * 100;
        
        if (change > 5) return "INCREASING";
        if (change < -5) return "DECREASING";
        return "STABLE";
    }
    
    private double getTrendFactor(String trend) {
        switch (trend) {
            case "INCREASING": return 0.5;  // +0.5% par jour
            case "DECREASING": return -0.5; // -0.5% par jour
            default: return 0.0;
        }
    }
    
    private String generateForecastInsight(String trend, double avgProduction, double avgQuality) {
        String trendText = trend.equals("INCREASING") ? "en hausse" 
                : trend.equals("DECREASING") ? "en baisse" 
                : "stable";
        
        return String.format("Basé sur les 30 derniers jours (moyenne: %.0f unités/jour, qualité: %.1f%%), " +
                "la tendance est %s. Niveau de confiance élevé pour les 3 prochains jours, " +
                "décroissant ensuite.", avgProduction, avgQuality, trendText);
    }
    
    private String generateProductionDropRecommendation(double deviation) {
        if (deviation > 30) {
            return "URGENT : Arrêt probable de production. Vérifier immédiatement la ligne et les équipements.";
        } else if (deviation > 15) {
            return "Chute significative. Inspecter la ligne, vérifier les matières premières et le personnel.";
        } else {
            return "Baisse modérée. Surveiller et vérifier si la tendance se poursuit.";
        }
    }
    
    private String generateDefectSpikeRecommendation(double deviation) {
        if (deviation > 100) {
            return "CRITIQUE : Pic majeur de défauts. Suspendre la production et inspecter immédiatement.";
        } else if (deviation > 50) {
            return "Pic important. Contrôle qualité renforcé et vérification des procédures recommandé.";
        } else {
            return "Augmentation des défauts. Surveiller de près et identifier les causes possibles.";
        }
    }
}
