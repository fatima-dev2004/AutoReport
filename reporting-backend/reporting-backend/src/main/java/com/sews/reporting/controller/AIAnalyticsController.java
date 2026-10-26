package com.sews.reporting.controller;

import com.sews.reporting.dto.AnomalyDetectionDto;
import com.sews.reporting.dto.ProductionForecastDto;
import com.sews.reporting.dto.RecommendationDto;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.AuditLogService;
import com.sews.reporting.service.ProductionAIService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour les fonctionnalités d'IA analytique
 * 
 * Implémente le CDC Section 8 - Évolutions possibles :
 * - Détection automatique des anomalies
 * - Prédiction des tendances de production
 * - Génération de recommandations d'amélioration
 * 
 * Accessible par ADMIN et MANAGER
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Slf4j
public class AIAnalyticsController {

    private final ProductionAIService aiService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    /**
     * Détecte les anomalies dans les données de production
     * GET /api/ai/anomalies?start=2024-01-01&end=2024-01-31
     */
    @GetMapping("/anomalies")
    public ResponseEntity<Map<String, Object>> detectAnomalies(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            HttpServletRequest request) {
        
        log.info("Détection des anomalies demandée : {} - {}", start, end);
        
        List<AnomalyDetectionDto> anomalies = aiService.detectAnomalies(start, end);
        
        // Statistiques par sévérité
        long critical = anomalies.stream()
                .filter(a -> a.getSeverity() == AnomalyDetectionDto.Severity.CRITICAL)
                .count();
        long high = anomalies.stream()
                .filter(a -> a.getSeverity() == AnomalyDetectionDto.Severity.HIGH)
                .count();
        long medium = anomalies.stream()
                .filter(a -> a.getSeverity() == AnomalyDetectionDto.Severity.MEDIUM)
                .count();
        long low = anomalies.stream()
                .filter(a -> a.getSeverity() == AnomalyDetectionDto.Severity.LOW)
                .count();
        
        Map<String, Object> response = new HashMap<>();
        response.put("anomalies", anomalies);
        response.put("totalCount", anomalies.size());
        response.put("statistics", Map.of(
                "critical", critical,
                "high", high,
                "medium", medium,
                "low", low
        ));
        response.put("period", Map.of("start", start, "end", end));
        
        // Audit log
        User currentUser = getCurrentUserOrNull();
        auditLogService.logFromRequest(
                request,
                currentUser != null ? currentUser.getId() : null,
                AuditLog.ActionType.OTHER,
                "Détection anomalies IA",
                String.format("Période: %s - %s, Anomalies trouvées: %d", start, end, anomalies.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Génère des prédictions de production
     * GET /api/ai/predictions?days=7
     */
    @GetMapping("/predictions")
    public ResponseEntity<ProductionForecastDto> predictProduction(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        
        log.info("Prédiction de production demandée pour {} jours", days);
        
        if (days < 1 || days > 30) {
            throw new IllegalArgumentException("Le nombre de jours doit être entre 1 et 30");
        }
        
        ProductionForecastDto forecast = aiService.predictProduction(days);
        
        // Audit log
        User currentUser = getCurrentUserOrNull();
        auditLogService.logFromRequest(
                request,
                currentUser != null ? currentUser.getId() : null,
                AuditLog.ActionType.OTHER,
                "Prédiction production IA",
                String.format("Prédiction sur %d jours, Tendance: %s", days, forecast.getTrend())
        );
        
        return ResponseEntity.ok(forecast);
    }

    /**
     * Génère des recommandations d'amélioration
     * GET /api/ai/recommendations
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> generateRecommendations(HttpServletRequest request) {
        
        log.info("Génération de recommandations demandée");
        
        List<RecommendationDto> recommendations = aiService.generateRecommendations();
        
        // Statistiques par priorité
        long urgent = recommendations.stream()
                .filter(r -> r.getPriority() == RecommendationDto.Priority.URGENT)
                .count();
        long high = recommendations.stream()
                .filter(r -> r.getPriority() == RecommendationDto.Priority.HIGH)
                .count();
        long medium = recommendations.stream()
                .filter(r -> r.getPriority() == RecommendationDto.Priority.MEDIUM)
                .count();
        long low = recommendations.stream()
                .filter(r -> r.getPriority() == RecommendationDto.Priority.LOW)
                .count();
        
        Map<String, Object> response = new HashMap<>();
        response.put("recommendations", recommendations);
        response.put("totalCount", recommendations.size());
        response.put("statistics", Map.of(
                "urgent", urgent,
                "high", high,
                "medium", medium,
                "low", low
        ));
        
        // Audit log
        User currentUser = getCurrentUserOrNull();
        auditLogService.logFromRequest(
                request,
                currentUser != null ? currentUser.getId() : null,
                AuditLog.ActionType.OTHER,
                "Recommandations IA",
                String.format("Recommandations générées: %d", recommendations.size())
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Tableau de bord IA complet (anomalies + prédictions + recommandations)
     * GET /api/ai/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAIDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "7") int forecastDays,
            HttpServletRequest request) {
        
        log.info("Dashboard IA complet demandé");
        
        // Dates par défaut : 30 derniers jours
        if (start == null) start = LocalDate.now().minusDays(30);
        if (end == null) end = LocalDate.now();
        
        // Récupérer toutes les données
        List<AnomalyDetectionDto> anomalies = aiService.detectAnomalies(start, end);
        ProductionForecastDto forecast = aiService.predictProduction(forecastDays);
        List<RecommendationDto> recommendations = aiService.generateRecommendations();
        
        // Construire le dashboard
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("anomalies", Map.of(
                "data", anomalies,
                "count", anomalies.size(),
                "critical", anomalies.stream()
                        .filter(a -> a.getSeverity() == AnomalyDetectionDto.Severity.CRITICAL)
                        .count()
        ));
        dashboard.put("forecast", forecast);
        dashboard.put("recommendations", Map.of(
                "data", recommendations,
                "count", recommendations.size(),
                "urgent", recommendations.stream()
                        .filter(r -> r.getPriority() == RecommendationDto.Priority.URGENT)
                        .count()
        ));
        dashboard.put("generatedAt", java.time.LocalDateTime.now());
        
        // Audit log
        User currentUser = getCurrentUserOrNull();
        auditLogService.logFromRequest(
                request,
                currentUser != null ? currentUser.getId() : null,
                AuditLog.ActionType.OTHER,
                "Dashboard IA complet",
                String.format("Anomalies: %d, Recommandations: %d", anomalies.size(), recommendations.size())
        );
        
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Informations sur les capacités IA
     * GET /api/ai/info
     */
    @GetMapping("/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Map<String, Object>> getAIInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", "1.0.0");
        info.put("features", List.of(
                "Détection d'anomalies (algorithme Z-score)",
                "Prédiction de production (moyenne mobile avec tendance)",
                "Recommandations d'amélioration automatiques"
        ));
        info.put("algorithms", Map.of(
                "anomalyDetection", "Z-Score Statistical Analysis",
                "forecasting", "Moving Average with Trend Detection",
                "recommendations", "Rule-Based Expert System"
        ));
        info.put("thresholds", Map.of(
                "anomalyZScore", 2.0,
                "criticalZScore", 3.0,
                "targetQualityRate", 95.0
        ));
        
        return ResponseEntity.ok(info);
    }

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
