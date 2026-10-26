package com.sews.reporting.controller;

import com.sews.reporting.dto.ReportAuthorizationDto;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.ReportAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la gestion des autorisations d'accès aux rapports
 * Accessible uniquement par les ADMIN et MANAGER
 * 
 * Implémente le CDC Section 5.1 : contrôle d'accès fin aux rapports
 */
@RestController
@RequestMapping("/api/admin/report-authorizations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Slf4j
public class AdminReportAuthorizationController {

    private final ReportAuthorizationService authorizationService;
    private final UserRepository userRepository;

    /**
     * Autoriser un utilisateur à accéder à un rapport
     * POST /api/admin/report-authorizations
     */
    @PostMapping
    public ResponseEntity<ReportAuthorizationDto> authorizeUser(
            @RequestBody AuthorizeUserRequest request) {
        
        User currentUser = getCurrentUser();
        
        ReportAuthorizationDto authorization = authorizationService.authorizeUserForReport(
            request.getReportId(),
            request.getUserId(),
            currentUser.getId(),
            request.getReason()
        );
        
        log.info("Utilisateur {} autorisé pour le rapport {} par {}", 
                request.getUserId(), request.getReportId(), currentUser.getEmail());
        
        return ResponseEntity.ok(authorization);
    }

    /**
     * Autoriser plusieurs utilisateurs en une seule fois
     * POST /api/admin/report-authorizations/bulk
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<ReportAuthorizationDto>> authorizeMultipleUsers(
            @RequestBody BulkAuthorizeRequest request) {
        
        User currentUser = getCurrentUser();
        
        List<ReportAuthorizationDto> authorizations = authorizationService.authorizeMultipleUsers(
            request.getReportId(),
            request.getUserIds(),
            currentUser.getId(),
            request.getReason()
        );
        
        log.info("{} utilisateurs autorisés pour le rapport {} par {}", 
                request.getUserIds().size(), request.getReportId(), currentUser.getEmail());
        
        return ResponseEntity.ok(authorizations);
    }

    /**
     * Révoquer l'autorisation d'un utilisateur pour un rapport
     * DELETE /api/admin/report-authorizations/{reportId}/users/{userId}
     */
    @DeleteMapping("/{reportId}/users/{userId}")
    public ResponseEntity<Map<String, String>> revokeAuthorization(
            @PathVariable Long reportId,
            @PathVariable Long userId) {
        
        authorizationService.revokeUserAuthorization(reportId, userId);
        
        log.info("Autorisation révoquée pour l'utilisateur {} sur le rapport {}", userId, reportId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Autorisation révoquée avec succès",
            "reportId", reportId.toString(),
            "userId", userId.toString()
        ));
    }

    /**
     * Récupérer toutes les autorisations pour un rapport spécifique
     * GET /api/admin/report-authorizations/reports/{reportId}
     */
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<List<ReportAuthorizationDto>> getAuthorizationsForReport(
            @PathVariable Long reportId) {
        
        List<ReportAuthorizationDto> authorizations = 
            authorizationService.getAuthorizationsForReport(reportId);
        
        return ResponseEntity.ok(authorizations);
    }

    /**
     * Récupérer toutes les autorisations d'un utilisateur
     * GET /api/admin/report-authorizations/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReportAuthorizationDto>> getAuthorizationsForUser(
            @PathVariable Long userId) {
        
        List<ReportAuthorizationDto> authorizations = 
            authorizationService.getAuthorizationsForUser(userId);
        
        return ResponseEntity.ok(authorizations);
    }

    /**
     * Vérifier si un utilisateur a accès à un rapport
     * GET /api/admin/report-authorizations/check?reportId=X&userId=Y
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAuthorization(
            @RequestParam Long reportId,
            @RequestParam Long userId) {
        
        boolean isAuthorized = authorizationService.isUserAuthorized(reportId, userId);
        
        return ResponseEntity.ok(Map.of("authorized", isAuthorized));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    // ===== DTOs internes =====

    @lombok.Data
    public static class AuthorizeUserRequest {
        private Long reportId;
        private Long userId;
        private String reason;
    }

    @lombok.Data
    public static class BulkAuthorizeRequest {
        private Long reportId;
        private List<Long> userIds;
        private String reason;
    }
}
