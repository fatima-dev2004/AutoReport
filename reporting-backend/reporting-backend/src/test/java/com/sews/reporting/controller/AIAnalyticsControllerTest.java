package com.sews.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sews.reporting.dto.AnomalyDetectionDto;
import com.sews.reporting.dto.ProductionForecastDto;
import com.sews.reporting.dto.RecommendationDto;
import com.sews.reporting.service.ProductionAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests pour AIAnalyticsController - endpoints d'analyse IA
 * 
 * Couvre:
 * - GET /api/ai/anomalies (détection d'anomalies)
 * - GET /api/ai/predictions (prédictions de production)
 * - GET /api/ai/recommendations (recommandations)
 * - GET /api/ai/dashboard (dashboard complet)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AIAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductionAIService aiService;

    // ========== Tests GET /api/ai/anomalies ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void detectAnomalies_WithValidDates_ReturnsAnomalies() throws Exception {
        AnomalyDetectionDto anomaly1 = new AnomalyDetectionDto();
        anomaly1.setDate(LocalDate.now().minusDays(2));
        anomaly1.setProductionLine("Line A");
        anomaly1.setType("PRODUCTION_DROP");
        anomaly1.setSeverity("HIGH");
        anomaly1.setDescription("Production dropped significantly");
        anomaly1.setActualValue(800.0);
        anomaly1.setExpectedValue(1000.0);
        anomaly1.setDeviation(-20.0);

        AnomalyDetectionDto anomaly2 = new AnomalyDetectionDto();
        anomaly2.setDate(LocalDate.now().minusDays(1));
        anomaly2.setProductionLine("Line B");
        anomaly2.setType("DEFECTS_SPIKE");
        anomaly2.setSeverity("CRITICAL");
        anomaly2.setDescription("Defect rate spike detected");
        anomaly2.setActualValue(15.0);
        anomaly2.setExpectedValue(5.0);
        anomaly2.setDeviation(200.0);

        when(aiService.detectAnomalies(any(), any(), any()))
                .thenReturn(Arrays.asList(anomaly1, anomaly2));

        mockMvc.perform(get("/api/ai/anomalies")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("PRODUCTION_DROP"))
                .andExpect(jsonPath("$[0].severity").value("HIGH"))
                .andExpect(jsonPath("$[1].type").value("DEFECTS_SPIKE"))
                .andExpect(jsonPath("$[1].severity").value("CRITICAL"));
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void detectAnomalies_WithProductionLine_ReturnsFilteredAnomalies() throws Exception {
        AnomalyDetectionDto anomaly = new AnomalyDetectionDto();
        anomaly.setDate(LocalDate.now());
        anomaly.setProductionLine("Line A");
        anomaly.setType("QUALITY_DEGRADATION");
        anomaly.setSeverity("MEDIUM");

        when(aiService.detectAnomalies(any(), any(), any()))
                .thenReturn(List.of(anomaly));

        mockMvc.perform(get("/api/ai/anomalies")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .param("productionLine", "Line A")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productionLine").value("Line A"));
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void detectAnomalies_AsUser_ReturnsAnomalies() throws Exception {
        when(aiService.detectAnomalies(any(), any(), any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/anomalies")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void detectAnomalies_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/ai/anomalies")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void detectAnomalies_WithInvalidDates_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/ai/anomalies")
                .param("startDate", "2026-08-01")
                .param("endDate", "2026-07-01") // End before start
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== Tests GET /api/ai/predictions ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void predictProduction_WithValidParams_ReturnsPredictions() throws Exception {
        ProductionForecastDto forecast = new ProductionForecastDto();
        forecast.setProductionLine("Line A");
        forecast.setForecastDate(LocalDate.now().plusDays(1));
        forecast.setPredictedProduction(1050.0);
        forecast.setConfidenceLevel(85.0);
        forecast.setTrend("INCREASING");
        forecast.setHistoricalAverage(1000.0);

        when(aiService.predictProduction(any(), any(), any()))
                .thenReturn(List.of(forecast));

        mockMvc.perform(get("/api/ai/predictions")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productionLine").value("Line A"))
                .andExpect(jsonPath("$[0].predictedProduction").value(1050.0))
                .andExpect(jsonPath("$[0].trend").value("INCREASING"));
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void predictProduction_WithDaysAhead_ReturnsMultiplePredictions() throws Exception {
        ProductionForecastDto day1 = new ProductionForecastDto();
        day1.setForecastDate(LocalDate.now().plusDays(1));
        day1.setPredictedProduction(1050.0);

        ProductionForecastDto day2 = new ProductionForecastDto();
        day2.setForecastDate(LocalDate.now().plusDays(2));
        day2.setPredictedProduction(1100.0);

        when(aiService.predictProduction(any(), any(), any()))
                .thenReturn(Arrays.asList(day1, day2));

        mockMvc.perform(get("/api/ai/predictions")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .param("daysAhead", "7")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void predictProduction_AsUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/ai/predictions")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========== Tests GET /api/ai/recommendations ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void getRecommendations_WithValidDates_ReturnsRecommendations() throws Exception {
        RecommendationDto rec1 = new RecommendationDto();
        rec1.setCategory("QUALITY_IMPROVEMENT");
        rec1.setPriority("HIGH");
        rec1.setTitle("Investigate quality issues on Line B");
        rec1.setDescription("Defect rate has increased significantly");
        rec1.setImpact("Potential to reduce defects by 50%");
        rec1.setActionItems(Arrays.asList("Check machine calibration", "Review operator training"));

        RecommendationDto rec2 = new RecommendationDto();
        rec2.setCategory("MAINTENANCE");
        rec2.setPriority("MEDIUM");
        rec2.setTitle("Schedule preventive maintenance");
        rec2.setDescription("Production efficiency declining");

        when(aiService.generateRecommendations(any(), any(), any()))
                .thenReturn(Arrays.asList(rec1, rec2));

        mockMvc.perform(get("/api/ai/recommendations")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].category").value("QUALITY_IMPROVEMENT"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[1].category").value("MAINTENANCE"));
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void getRecommendations_WithProductionLine_ReturnsFiltered() throws Exception {
        RecommendationDto rec = new RecommendationDto();
        rec.setCategory("PROCESS_IMPROVEMENT");
        rec.setPriority("LOW");
        rec.setProductionLine("Line A");

        when(aiService.generateRecommendations(any(), any(), any()))
                .thenReturn(List.of(rec));

        mockMvc.perform(get("/api/ai/recommendations")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .param("productionLine", "Line A")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productionLine").value("Line A"));
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void getRecommendations_AsUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/ai/recommendations")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // ========== Tests GET /api/ai/dashboard ==========

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void getAIDashboard_WithValidDates_ReturnsCompleteDashboard() throws Exception {
        // Mock anomalies
        AnomalyDetectionDto anomaly = new AnomalyDetectionDto();
        anomaly.setType("PRODUCTION_DROP");
        anomaly.setSeverity("HIGH");
        when(aiService.detectAnomalies(any(), any(), any()))
                .thenReturn(List.of(anomaly));

        // Mock predictions
        ProductionForecastDto forecast = new ProductionForecastDto();
        forecast.setPredictedProduction(1050.0);
        when(aiService.predictProduction(any(), any(), any()))
                .thenReturn(List.of(forecast));

        // Mock recommendations
        RecommendationDto recommendation = new RecommendationDto();
        recommendation.setCategory("QUALITY_IMPROVEMENT");
        recommendation.setPriority("HIGH");
        when(aiService.generateRecommendations(any(), any(), any()))
                .thenReturn(List.of(recommendation));

        mockMvc.perform(get("/api/ai/dashboard")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anomalies", hasSize(1)))
                .andExpect(jsonPath("$.predictions", hasSize(1)))
                .andExpect(jsonPath("$.recommendations", hasSize(1)))
                .andExpect(jsonPath("$.summary").exists())
                .andExpect(jsonPath("$.summary.totalAnomalies").value(1))
                .andExpect(jsonPath("$.summary.criticalIssues").exists())
                .andExpect(jsonPath("$.summary.avgPredictedProduction").exists());
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void getAIDashboard_AsManager_ReturnsSuccess() throws Exception {
        when(aiService.detectAnomalies(any(), any(), any())).thenReturn(List.of());
        when(aiService.predictProduction(any(), any(), any())).thenReturn(List.of());
        when(aiService.generateRecommendations(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/ai/dashboard")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anomalies").isArray())
                .andExpect(jsonPath("$.predictions").isArray())
                .andExpect(jsonPath("$.recommendations").isArray());
    }

    @Test
    @WithMockUser(username = "user@sews.com", roles = "USER")
    void getAIDashboard_AsUser_Returns403() throws Exception {
        mockMvc.perform(get("/api/ai/dashboard")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAIDashboard_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/ai/dashboard")
                .param("startDate", "2026-07-01")
                .param("endDate", "2026-07-31")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
