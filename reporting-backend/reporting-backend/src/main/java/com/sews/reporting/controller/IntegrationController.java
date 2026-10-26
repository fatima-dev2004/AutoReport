package com.sews.reporting.controller;

import com.sews.reporting.integration.ExternalDataSource;
import com.sews.reporting.integration.ExternalDataSourceException;
import com.sews.reporting.service.IntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur pour la gestion des intégrations avec les systèmes externes (SIGIP, etc.)
 * Accessible uniquement par les ADMIN
 * 
 * Permet de :
 * - Tester les connexions aux sources externes
 * - Synchroniser les données
 * - Consulter l'état des intégrations
 */
@RestController
@RequestMapping("/api/admin/integrations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class IntegrationController {

    private final IntegrationService integrationService;

    /**
     * Liste toutes les sources de données externes configurées
     * GET /api/admin/integrations
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listDataSources() {
        List<ExternalDataSource> sources = integrationService.getAllDataSources();
        
        List<Map<String, Object>> sourcesInfo = sources.stream()
                .map(source -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("name", source.getSourceName());
                    info.put("type", source.getSystemType());
                    info.put("available", source.isAvailable());
                    info.put("metadata", source.getConnectionMetadata());
                    return info;
                })
                .collect(Collectors.toList());
        
        ExternalDataSource activeSource = integrationService.getActiveDataSource();
        
        Map<String, Object> response = new HashMap<>();
        response.put("sources", sourcesInfo);
        response.put("activeSource", activeSource.getSourceName());
        response.put("totalSources", sources.size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Teste la connexion à la source de données active
     * GET /api/admin/integrations/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        ExternalDataSource source = integrationService.getActiveDataSource();
        
        log.info("Test de connexion demandé pour : {}", source.getSourceName());
        
        long startTime = System.currentTimeMillis();
        boolean success = source.testConnection();
        long duration = System.currentTimeMillis() - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("source", source.getSourceName());
        response.put("systemType", source.getSystemType());
        response.put("durationMs", duration);
        response.put("message", success 
                ? "Connexion établie avec succès" 
                : "Échec de la connexion");
        response.put("metadata", source.getConnectionMetadata());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Synchronise les données depuis la source externe
     * POST /api/admin/integrations/sync?start=2024-01-01&end=2024-01-31
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> synchronizeData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        ExternalDataSource source = integrationService.getActiveDataSource();
        
        log.info("Synchronisation demandée depuis {} : {} à {}", 
                source.getSourceName(), start, end);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            int recordsImported = integrationService.synchronizeFromExternal(start, end);
            long duration = System.currentTimeMillis() - startTime;
            
            response.put("success", true);
            response.put("source", source.getSourceName());
            response.put("recordsImported", recordsImported);
            response.put("period", Map.of("start", start, "end", end));
            response.put("durationMs", duration);
            response.put("message", String.format("%d enregistrements importés avec succès", recordsImported));
            
            log.info("Synchronisation terminée : {} enregistrements en {}ms", 
                    recordsImported, duration);
            
            return ResponseEntity.ok(response);
            
        } catch (ExternalDataSourceException e) {
            log.error("Erreur lors de la synchronisation", e);
            
            response.put("success", false);
            response.put("source", source.getSourceName());
            response.put("error", e.getMessage());
            response.put("message", "Échec de la synchronisation");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Récupère les métadonnées de connexion
     * GET /api/admin/integrations/metadata
     */
    @GetMapping("/metadata")
    public ResponseEntity<Map<String, Object>> getMetadata() {
        ExternalDataSource source = integrationService.getActiveDataSource();
        ExternalDataSource.ConnectionMetadata metadata = source.getConnectionMetadata();
        
        Map<String, Object> response = new HashMap<>();
        response.put("source", source.getSourceName());
        response.put("systemType", source.getSystemType());
        response.put("host", metadata.getHost());
        response.put("database", metadata.getDatabase());
        response.put("status", metadata.getStatus());
        response.put("lastSync", metadata.getLastSync());
        response.put("enabled", metadata.isEnabled());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Informations sur les capacités d'intégration
     * GET /api/admin/integrations/info
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getIntegrationInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("version", "1.0.0");
        info.put("supportedSystems", List.of(
                "SIGIP DB2 IBM i",
                "Mock/Simulation (développement)"
        ));
        info.put("features", List.of(
                "Test de connexion",
                "Synchronisation automatique des données",
                "Support multi-sources",
                "Architecture extensible"
        ));
        info.put("profiles", Map.of(
                "dev/demo", "Utilise MockSigipDataSource (données simulées)",
                "production", "Utilise SigipDB2DataSource (connexion DB2 réelle)"
        ));
        info.put("documentation", Map.of(
                "interface", "ExternalDataSource.java",
                "mockImpl", "MockSigipDataSource.java",
                "db2Impl", "SigipDB2DataSource.java (stub, à implémenter)"
        ));
        
        return ResponseEntity.ok(info);
    }
}
