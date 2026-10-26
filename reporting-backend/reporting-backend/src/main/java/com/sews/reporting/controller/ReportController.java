package com.sews.reporting.controller;

import com.sews.reporting.dto.ReportGenerationRequest;
import com.sews.reporting.dto.ReportSummaryDto;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.Role;
import com.sews.reporting.entity.User;
import com.sews.reporting.exception.ResourceNotFoundException;
import com.sews.reporting.exception.UnauthorizedAccessException;
import com.sews.reporting.repository.ProductionReportRepository;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.AuditLogService;
import com.sews.reporting.service.ExcelImportService;
import com.sews.reporting.service.ReportGenerationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Regroupe :
 * - 5.2 Importation des fichiers Excel
 * - 5.4 Generation automatique des rapports
 * - 5.7 Historique des rapports
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ExcelImportService excelImportService;
    private final ReportGenerationService reportGenerationService;
    private final ProductionReportRepository productionReportRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final com.sews.reporting.service.EmailService emailService;
    private final com.sews.reporting.service.ReportAuthorizationService reportAuthorizationService;
    private final com.sews.reporting.repository.ImportHistoryRepository importHistoryRepository;

    // 5.2 - Import du fichier Excel (Manager/Admin)
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('IMPORT_EXCEL')")
    public ResponseEntity<ExcelImportService.ImportResult> importExcel(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUserOrNull();
        ExcelImportService.ImportResult result = excelImportService.importFile(file);
        
        // Log de l'action
        auditLogService.logFromRequest(
            request,
            currentUser != null ? currentUser.getId() : null,
            AuditLog.ActionType.IMPORT_EXCEL,
            "Importation fichier Excel",
            "Fichier: " + file.getOriginalFilename() + ", Lignes importées: " + result.getRecordsImported()
        );

        // Historique persistant de l'import (centralise, visible par l'administrateur)
        com.sews.reporting.entity.ImportHistory.Status status;
        if (result.getRecordsImported() == 0 && !result.getErrors().isEmpty()) {
            status = com.sews.reporting.entity.ImportHistory.Status.FAILED;
        } else if (!result.getErrors().isEmpty()) {
            status = com.sews.reporting.entity.ImportHistory.Status.PARTIAL;
        } else {
            status = com.sews.reporting.entity.ImportHistory.Status.SUCCESS;
        }
        String errorDetails = result.getErrors().stream()
                .reduce((a, b) -> a + "\n" + b)
                .orElse(null);

        importHistoryRepository.save(com.sews.reporting.entity.ImportHistory.builder()
                .fileName(file.getOriginalFilename())
                .user(currentUser)
                .importDate(result.getImportedAt())
                .status(status)
                .rowsTotal(result.getTotalRows())
                .rowsImported(result.getRecordsImported())
                .rowsSkipped(result.getRowsSkipped())
                .errorCount(result.getErrors().size())
                .errorDetails(errorDetails)
                .build());
        
        return ResponseEntity.ok(result);
    }

    // Historique persistant des imports (ADMIN/MANAGER) - remplace le localStorage frontend
    @GetMapping("/import/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<org.springframework.data.domain.Page<com.sews.reporting.entity.ImportHistory>> getImportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(importHistoryRepository.findAllByOrderByImportDateDesc(
                org.springframework.data.domain.PageRequest.of(page, size)));
    }

    // Options de filtres pour la page Reports du frontend (types, departements)
    @GetMapping("/filters")
    public ResponseEntity<java.util.Map<String, Object>> getFilters() {
        java.util.Map<String, Object> filters = new java.util.LinkedHashMap<>();
        filters.put("types", ProductionReport.ReportType.values());
        filters.put("departments", productionReportRepository.findDistinctDepartments());
        return ResponseEntity.ok(filters);
    }

    // 5.4 - Génération d'un rapport SIMPLE (ancienne version)
    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('GENERATE_REPORTS')")
    public ResponseEntity<ReportSummaryDto> generateReport(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end,
            HttpServletRequest request) {

        User currentUser = getCurrentUserOrNull();
        ProductionReport report = reportGenerationService.generateProductionReport(start, end, currentUser);
        
        // Log de l'action
        auditLogService.logFromRequest(
            request,
            currentUser != null ? currentUser.getId() : null,
            AuditLog.ActionType.GENERATE_REPORT,
            "Génération de rapport simple",
            "Période: " + start + " - " + end
        );
        
        return ResponseEntity.ok(toDto(report));
    }
    
    // NOUVEAU - Génération de rapport AMÉLIORÉ avec page de garde et graphiques
    @PostMapping("/generate/enhanced")
    public ResponseEntity<ReportSummaryDto> generateEnhancedReport(
            @RequestBody ReportGenerationRequest request,
            HttpServletRequest httpRequest) {

        User currentUser = getCurrentUserOrNull();
        ProductionReport report = reportGenerationService.generateEnhancedReport(request, currentUser);
        
        // Log de l'action
        auditLogService.logFromRequest(
            httpRequest,
            currentUser != null ? currentUser.getId() : null,
            AuditLog.ActionType.GENERATE_REPORT,
            "Génération de rapport amélioré",
            "Titre: " + request.getReportTitle() + 
            ", Période: " + request.getStartDate() + " - " + request.getEndDate() +
            ", Page de garde: " + (request.getIncludeCoverPage() != null && request.getIncludeCoverPage())
        );
        
        return ResponseEntity.ok(toDto(report));
    }

    // 5.7 - Historique : liste de tous les rapports (avec filtrage par rôle) - AVEC PAGINATION
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<ReportSummaryDto>> listReports(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "generatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        User currentUser = getCurrentUserOrNull();
        
        // Créer l'objet Pageable pour la pagination
        org.springframework.data.domain.Sort.Direction direction = 
            sortDirection.equalsIgnoreCase("ASC") 
                ? org.springframework.data.domain.Sort.Direction.ASC 
                : org.springframework.data.domain.Sort.Direction.DESC;
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, direction, sortBy);
        
        org.springframework.data.domain.Page<ProductionReport> reports;
        
        // Filtrage selon le rôle de l'utilisateur (CONFORME CDC Section 5.1)
        if (currentUser != null && currentUser.getRole() == Role.USER) {
            // Les employés (USER) ne voient QUE les rapports autorisés :
            // - rapports qu'ils ont créés
            // - rapports de leur département
            // - rapports avec autorisation explicite via ReportAuthorization
            String userDepartment = currentUser.getDepartment() != null ? currentUser.getDepartment() : "";
            
            reports = productionReportRepository.findAuthorizedReportsForUser(
                currentUser.getId(), 
                userDepartment, 
                pageable
            );
        } else {
            // Les ADMIN et MANAGER voient tous les rapports
            reports = (search == null || search.isBlank())
                    ? productionReportRepository.findAll(pageable)
                    : productionReportRepository.findByTitleContainingIgnoreCase(search, pageable);
        }

        // Convertir Page<ProductionReport> en Page<ReportSummaryDto>
        org.springframework.data.domain.Page<ReportSummaryDto> dtoPage = 
            reports.map(this::toDto);

        return ResponseEntity.ok(dtoPage);
    }

    // 5.7 - Telechargement du PDF d'un rapport precedent
    @GetMapping("/{id}/download/pdf")
    @PreAuthorize("hasPermission(#id, 'ProductionReport', 'DOWNLOAD_REPORTS')")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        // Vérifier l'accès pour USER
        checkReportAccess(id);
        return download(id, true);
    }

    // 5.4 - Telechargement du fichier Excel genere
    @GetMapping("/{id}/download/excel")
    @PreAuthorize("hasPermission(#id, 'ProductionReport', 'DOWNLOAD_REPORTS')")
    public ResponseEntity<Resource> downloadExcel(@PathVariable Long id) {
        // Vérifier l'accès pour USER
        checkReportAccess(id);
        return download(id, false);
    }
    
    /**
     * Vérifie si l'utilisateur actuel a le droit d'accéder à un rapport
     * Conforme CDC Section 5.1
     */
    private void checkReportAccess(Long reportId) {
        User currentUser = getCurrentUserOrNull();
        if (currentUser == null) {
            throw new UnauthorizedAccessException("Utilisateur non authentifié");
        }
        
        // ADMIN et MANAGER ont accès à tout
        if (currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MANAGER) {
            return;
        }
        
        // Pour les USER, vérifier l'autorisation
        String userDepartment = currentUser.getDepartment() != null ? currentUser.getDepartment() : "";
        boolean hasAccess = productionReportRepository.canUserAccessReport(
            reportId, 
            currentUser.getId(), 
            userDepartment
        );
        
        if (!hasAccess) {
            throw new UnauthorizedAccessException("Rapport", reportId);
        }
    }

    /**
     * Suppression d'un rapport (Admin/Manager uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#id, 'ProductionReport', 'DELETE')")
    public ResponseEntity<?> deleteReport(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = getCurrentUserOrNull();
        
        ProductionReport report = productionReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport", id));
        
        // MANAGER peut supprimer ses propres rapports ou ceux de son département
        if (currentUser != null && currentUser.getRole() == Role.MANAGER) {
            boolean isCreator = report.getGeneratedBy() != null && 
                               report.getGeneratedBy().getId().equals(currentUser.getId());
            boolean sameDepartment = currentUser.getDepartment() != null && 
                                    currentUser.getDepartment().equals(report.getDepartment());
            
            if (!isCreator && !sameDepartment) {
                throw new UnauthorizedAccessException("Vous ne pouvez pas supprimer ce rapport");
            }
        }
        
        // Supprimer fichiers physiques
        deleteFileIfExists(report.getPdfFilePath());
        deleteFileIfExists(report.getExcelFilePath());
        productionReportRepository.delete(report);
        
        auditLogService.logFromRequest(request, currentUser != null ? currentUser.getId() : null,
            AuditLog.ActionType.DELETE_DATA, "Suppression rapport", "ID: " + id);
        
        return ResponseEntity.ok(java.util.Map.of("message", "Rapport supprimé"));
    }

    private boolean deleteFileIfExists(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        try {
            File file = new File(filePath);
            return file.exists() && file.delete();
        } catch (Exception e) {
            log.error("Erreur suppression fichier : {}", filePath, e);
            return false;
        }
    }

    private ResponseEntity<Resource> download(Long id, boolean pdf) {
        ProductionReport report = productionReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rapport", id));

        String path = pdf ? report.getPdfFilePath() : report.getExcelFilePath();
        File file = new File(path);
        if (!file.exists()) {
            throw new ResourceNotFoundException("Fichier de rapport introuvable sur le serveur : " + file.getName());
        }

        Resource resource = new FileSystemResource(file);
        MediaType mediaType = pdf ? MediaType.APPLICATION_PDF
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    private ReportSummaryDto toDto(ProductionReport r) {
        return ReportSummaryDto.builder()
                .id(r.getId())
                .title(r.getTitle())
                .type(r.getType().name())
                .periodStart(r.getPeriodStart())
                .periodEnd(r.getPeriodEnd())
                .totalProduction(r.getTotalProduction())
                .totalDefects(r.getTotalDefects())
                .qualityRate(r.getQualityRate())
                .generatedByEmail(r.getGeneratedBy() != null ? r.getGeneratedBy().getEmail() : "systeme (automatique)")
                .generatedAt(r.getGeneratedAt())
                .build();
    }

    private User getCurrentUserOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
    
    /**
     * NOUVEAU - Envoi ponctuel d'un rapport à un email spécifique
     * Permet à un admin d'envoyer un rapport à n'importe quel email sans l'ajouter à la liste permanente
     */
    @PostMapping("/{id}/send-to")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<java.util.Map<String, Object>> sendReportToEmail(
            @PathVariable Long id,
            @RequestParam String email,
            @RequestParam(required = false) String recipientName,
            HttpServletRequest request) {
        
        User currentUser = getCurrentUserOrNull();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            // Vérifier que le rapport existe
            ProductionReport report = productionReportRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Rapport", id));
            
            // Vérifier que le fichier PDF existe
            if (report.getPdfFilePath() == null || !new File(report.getPdfFilePath()).exists()) {
                response.put("success", false);
                response.put("message", "Le fichier PDF du rapport n'existe pas");
                return ResponseEntity.status(400).body(response);
            }
            
            // Préparer le contenu de l'email
            String recipientDisplay = (recipientName != null && !recipientName.isBlank()) 
                ? recipientName + " (" + email + ")"
                : email;
            
            String subject = "Rapport de Production - " + report.getTitle();
            String body = "Bonjour" + (recipientName != null && !recipientName.isBlank() ? " " + recipientName : "") + ",\n\n" +
                    "Veuillez trouver ci-joint le rapport de production suivant :\n\n" +
                    "Titre : " + report.getTitle() + "\n" +
                    "Période : " + report.getPeriodStart() + " au " + report.getPeriodEnd() + "\n" +
                    "Production totale : " + report.getTotalProduction() + " unités\n" +
                    "Taux de qualité : " + String.format("%.2f", report.getQualityRate()) + "%\n\n" +
                    "Cordialement,\n" +
                    "SEWS Reporting System";
            
            // Envoyer l'email
            emailService.sendReportByEmail(
                new String[]{email},
                subject,
                body,
                report.getPdfFilePath()
            );
            
            // Logger l'action
            auditLogService.logFromRequest(
                request,
                currentUser != null ? currentUser.getId() : null,
                AuditLog.ActionType.SEND_EMAIL,
                "Envoi ponctuel de rapport",
                "Rapport: " + report.getTitle() + ", Destinataire: " + recipientDisplay
            );
            
            response.put("success", true);
            response.put("message", "Rapport envoyé avec succès à " + recipientDisplay);
            
            log.info("Rapport {} envoyé à {}", id, email);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi du rapport {} à {}", id, email, e);
            response.put("success", false);
            response.put("message", "Erreur lors de l'envoi : " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
