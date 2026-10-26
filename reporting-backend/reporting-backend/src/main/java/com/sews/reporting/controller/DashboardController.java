package com.sews.reporting.controller;

import com.sews.reporting.dto.ReportGenerationRequest;
import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.ProductionRecordRepository;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Section 5.5 : Tableau de bord (Dashboard)
 * - graphiques, statistiques, indicateurs KPI
 * Exemples : production journaliere, taux de defaut, comparaison objectif/realisation
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProductionRecordRepository productionRecordRepository;
    private final ReportGenerationService reportGenerationService;
    private final UserRepository userRepository;

    @GetMapping("/kpis")
    public Map<String, Object> getKpis(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {

        LocalDate periodEnd = end != null ? end : LocalDate.now();
        LocalDate periodStart = start != null ? start : periodEnd.minusDays(30);

        List<ProductionRecord> records = productionRecordRepository.findByProductionDateBetween(periodStart, periodEnd);

        double total = records.stream().mapToDouble(ProductionRecord::getQuantity).sum();
        int defects = records.stream().mapToInt(ProductionRecord::getDefects).sum();
        double qualityRate = total > 0 ? Math.round(((total - defects) / total) * 10000.0) / 100.0 : 0.0;
        double defectRate = total > 0 ? Math.round((defects / total) * 10000.0) / 100.0 : 0.0;

        // Production journaliere (pour graphique en courbe)
        Map<LocalDate, Double> dailyProduction = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getProductionDate,
                        LinkedHashMap::new, Collectors.summingDouble(ProductionRecord::getQuantity)));

        // Production par ligne (pour graphique en barres)
        Map<String, Double> productionByLine = records.stream()
                .collect(Collectors.groupingBy(ProductionRecord::getLine,
                        LinkedHashMap::new, Collectors.summingDouble(ProductionRecord::getQuantity)));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("periodStart", periodStart);
        result.put("periodEnd", periodEnd);
        result.put("totalProduction", total);
        result.put("totalDefects", defects);
        result.put("qualityRate", qualityRate);
        result.put("defectRate", defectRate);
        result.put("dailyProduction", dailyProduction);
        result.put("productionByLine", productionByLine);

        return result;
    }
    
    /**
     * NOUVEAU - Générer et télécharger directement un rapport depuis le dashboard
     * Endpoint simplifié pour téléchargement rapide
     */
    @PostMapping("/download-report")
    public ResponseEntity<Resource> downloadReportFromDashboard(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "true") boolean enhanced,
            @RequestParam(required = false) String title) {
        
        LocalDate periodEnd = end != null ? end : LocalDate.now();
        LocalDate periodStart = start != null ? start : periodEnd.minusDays(30);
        
        User currentUser = getCurrentUserOrNull();
        
        ProductionReport report;
        
        if (enhanced) {
            // Générer rapport amélioré avec page de garde
            ReportGenerationRequest request = ReportGenerationRequest.builder()
                    .startDate(periodStart)
                    .endDate(periodEnd)
                    .reportTitle(title != null ? title : "Rapport Dashboard - " + periodEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .generatedBy(currentUser != null ? currentUser.getFullName() : "Dashboard")
                    .includeCoverPage(true)
                    .includeCharts(true)
                    .includeDetailedData(true)
                    .reportType("PRODUCTION")
                    .build();
            
            report = reportGenerationService.generateEnhancedReport(request, currentUser);
        } else {
            // Générer rapport simple
            report = reportGenerationService.generateProductionReport(periodStart, periodEnd, currentUser);
        }
        
        // Télécharger le PDF généré
        String pdfPath = report.getPdfFilePath();
        File file = new File(pdfPath);
        
        if (!file.exists()) {
            throw new RuntimeException("Fichier PDF introuvable");
        }
        
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
    
    /**
     * NOUVEAU - Générer le rapport et retourner l'ID pour téléchargement ultérieur
     */
    @PostMapping("/generate-report")
    public ResponseEntity<Map<String, Object>> generateReportFromDashboard(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "true") boolean enhanced,
            @RequestParam(required = false) String title) {
        
        LocalDate periodEnd = end != null ? end : LocalDate.now();
        LocalDate periodStart = start != null ? start : periodEnd.minusDays(30);
        
        User currentUser = getCurrentUserOrNull();
        
        ProductionReport report;
        
        if (enhanced) {
            ReportGenerationRequest request = ReportGenerationRequest.builder()
                    .startDate(periodStart)
                    .endDate(periodEnd)
                    .reportTitle(title != null ? title : "Rapport Dashboard - " + periodEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .generatedBy(currentUser != null ? currentUser.getFullName() : "Dashboard")
                    .includeCoverPage(true)
                    .includeCharts(true)
                    .includeDetailedData(true)
                    .reportType("PRODUCTION")
                    .build();
            
            report = reportGenerationService.generateEnhancedReport(request, currentUser);
        } else {
            report = reportGenerationService.generateProductionReport(periodStart, periodEnd, currentUser);
        }
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Rapport généré avec succès");
        response.put("reportId", report.getId());
        response.put("title", report.getTitle());
        response.put("downloadUrl", "/api/reports/" + report.getId() + "/download/pdf");
        response.put("downloadUrlExcel", "/api/reports/" + report.getId() + "/download/excel");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * NOUVEAU - Export Dashboard en Excel
     * Exporte les KPIs et graphiques actuels du dashboard en format Excel
     */
    @GetMapping("/export/excel")
    public ResponseEntity<Resource> exportDashboardToExcel(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        
        LocalDate periodEnd = end != null ? end : LocalDate.now();
        LocalDate periodStart = start != null ? start : periodEnd.minusDays(30);
        
        User currentUser = getCurrentUserOrNull();
        
        // Générer le rapport avec les données actuelles du dashboard
        ProductionReport report = reportGenerationService.generateProductionReport(periodStart, periodEnd, currentUser);
        
        String excelPath = report.getExcelFilePath();
        File file = new File(excelPath);
        
        if (!file.exists()) {
            throw new RuntimeException("Fichier Excel introuvable");
        }
        
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dashboard_" + periodEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".xlsx\"")
                .body(resource);
    }
    
    /**
     * NOUVEAU - Export Dashboard en PDF
     * Exporte les KPIs et graphiques actuels du dashboard en format PDF
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<Resource> exportDashboardToPdf(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end) {
        
        LocalDate periodEnd = end != null ? end : LocalDate.now();
        LocalDate periodStart = start != null ? start : periodEnd.minusDays(30);
        
        User currentUser = getCurrentUserOrNull();
        
        // Générer le rapport amélioré avec page de garde
        ReportGenerationRequest request = ReportGenerationRequest.builder()
                .startDate(periodStart)
                .endDate(periodEnd)
                .reportTitle("Export Dashboard - " + periodEnd.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .generatedBy(currentUser != null ? currentUser.getFullName() : "Dashboard Export")
                .includeCoverPage(true)
                .includeCharts(true)
                .includeDetailedData(true)
                .reportType("PRODUCTION")
                .build();
        
        ProductionReport report = reportGenerationService.generateEnhancedReport(request, currentUser);
        
        String pdfPath = report.getPdfFilePath();
        File file = new File(pdfPath);
        
        if (!file.exists()) {
            throw new RuntimeException("Fichier PDF introuvable");
        }
        
        Resource resource = new FileSystemResource(file);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dashboard_" + periodEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf\"")
                .body(resource);
    }
    
    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
