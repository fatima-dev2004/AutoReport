package com.sews.reporting.controller;

import com.sews.reporting.dto.SystemSettingsDto;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.entity.EmailRecipient;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.EmailRecipientRepository;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.AuditLogService;
import com.sews.reporting.service.EmailService;
import com.sews.reporting.service.SystemSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SystemSettingsController {

    private static final long MAX_LOGO_SIZE = 40 * 1024; // 40 Ko, pour rester sous la limite TEXT en base

    private final SystemSettingsService settingsService;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final EmailRecipientRepository emailRecipientRepository;
    private final UserRepository userRepository;
    
    /**
     * Helper method pour récupérer l'utilisateur depuis le principal
     */
    private User getUserFromPrincipal(org.springframework.security.core.userdetails.User principal) {
        return userRepository.findByEmail(principal.getUsername())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSettingsDto> getSettings() {
        SystemSettingsDto settings = settingsService.getAllSettings();
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> updateSettings(
            @RequestBody SystemSettingsDto settingsDto,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        User user = getUserFromPrincipal(principal);
        settingsService.updateSettings(settingsDto, user.getId());
        
        // Log the action
        auditLogService.logFromRequest(
            request,
            user.getId(),
            AuditLog.ActionType.UPDATE_SETTINGS,
            "Mise à jour des paramètres système",
            "Paramètres modifiés: SMTP, Entreprise, Planification"
        );
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Paramètres mis à jour avec succès");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testEmailSettings(
            @RequestParam String recipient,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            emailService.sendTestEmail(recipient);
            User user = getUserFromPrincipal(principal);
            
            // Log the action
            auditLogService.logFromRequest(
                request,
                user.getId(),
                AuditLog.ActionType.SEND_EMAIL,
                "Test d'envoi d'email",
                "Email de test envoyé à: " + recipient
            );
            
            response.put("success", true);
            response.put("message", "Email de test envoyé avec succès à " + recipient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Échec de l'envoi de l'email: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== Configuration SMTP ====================

    @GetMapping("/smtp")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getSmtpConfig() {
        Map<String, String> smtp = new HashMap<>();
        smtp.put("host", settingsService.getSetting("smtp.host", ""));
        smtp.put("port", settingsService.getSetting("smtp.port", "587"));
        smtp.put("username", settingsService.getSetting("smtp.username", ""));
        smtp.put("password", settingsService.getSetting("smtp.password", ""));
        smtp.put("fromName", settingsService.getSetting("smtp.fromName", ""));
        smtp.put("fromEmail", settingsService.getSetting("smtp.from", ""));
        return ResponseEntity.ok(smtp);
    }

    @PutMapping("/smtp")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> saveSmtpConfig(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        User user = getUserFromPrincipal(principal);
        Long userId = user.getId();
        settingsService.setSetting("smtp.host", body.getOrDefault("host", ""), "smtp", userId);
        settingsService.setSetting("smtp.port", body.getOrDefault("port", "587"), "smtp", userId);
        settingsService.setSetting("smtp.username", body.getOrDefault("username", ""), "smtp", userId);
        if (body.get("password") != null && !body.get("password").isBlank()) {
            settingsService.setSetting("smtp.password", body.get("password"), "smtp", userId);
        }
        settingsService.setSetting("smtp.fromName", body.getOrDefault("fromName", ""), "smtp", userId);
        settingsService.setSetting("smtp.from", body.getOrDefault("fromEmail", ""), "smtp", userId);

        auditLogService.logFromRequest(request, userId, AuditLog.ActionType.UPDATE_SETTINGS,
                "Mise à jour de la configuration SMTP", "Hôte : " + body.getOrDefault("host", ""));

        Map<String, String> response = new HashMap<>();
        response.put("message", "Configuration SMTP enregistrée");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/smtp/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testSmtpConfig(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal
    ) {
        User user = getUserFromPrincipal(principal);
        Map<String, Object> response = new HashMap<>();
        // Envoie le test au compte connecté, ou à défaut à l'adresse SMTP configurée
        String recipient = user.getEmail() != null ? user.getEmail() : settingsService.getSetting("smtp.username");
        if (recipient == null || recipient.isBlank()) {
            response.put("success", false);
            response.put("message", "Aucun destinataire disponible pour le test. Renseignez d'abord un email utilisateur ou SMTP.");
            return ResponseEntity.status(400).body(response);
        }
        try {
            emailService.sendTestEmail(recipient);
            response.put("success", true);
            response.put("message", "Email de test envoyé à " + recipient);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ==================== Configuration Planification ====================

    @GetMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getScheduleConfig() {
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("autoSendEnabled", Boolean.parseBoolean(settingsService.getSetting("scheduling.autoSendEnabled", "true")));
        schedule.put("sendTime", settingsService.getSetting("scheduling.sendTime", "08:00"));
        String days = settingsService.getSetting("scheduling.sendDays", "monday,tuesday,wednesday,thursday,friday");
        schedule.put("sendDays", days.isBlank() ? List.of() : List.of(days.split(",")));
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, String>> saveScheduleConfig(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        User user = getUserFromPrincipal(principal);
        Long userId = user.getId();
        settingsService.setSetting("scheduling.autoSendEnabled",
                String.valueOf(body.getOrDefault("autoSendEnabled", true)), "scheduling", userId);
        settingsService.setSetting("scheduling.sendTime",
                String.valueOf(body.getOrDefault("sendTime", "08:00")), "scheduling", userId);
        Object daysObj = body.get("sendDays");
        String daysStr = daysObj instanceof List
                ? ((List<Object>) daysObj).stream().map(String::valueOf).collect(Collectors.joining(","))
                : "";
        settingsService.setSetting("scheduling.sendDays", daysStr, "scheduling", userId);

        auditLogService.logFromRequest(request, userId, AuditLog.ActionType.UPDATE_SETTINGS,
                "Mise à jour de la planification", "Jours : " + daysStr);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Planification enregistrée");
        return ResponseEntity.ok(response);
    }

    // ==================== Configuration Entreprise ====================

    @GetMapping("/company")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getCompanyConfig() {
        Map<String, String> company = new HashMap<>();
        company.put("name", settingsService.getSetting("company.name", ""));
        company.put("logo", settingsService.getSetting("company.logo", ""));
        return ResponseEntity.ok(company);
    }

    @PutMapping(value = "/company", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> saveCompanyConfigMultipart(
            @RequestParam("name") String name,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) throws IOException {
        User user = getUserFromPrincipal(principal);
        Long userId = user.getId();
        settingsService.setSetting("company.name", name, "company", userId);

        if (logo != null && !logo.isEmpty()) {
            if (logo.getSize() > MAX_LOGO_SIZE) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Le logo doit faire moins de 40 Ko. Utilisez une image plus légère.");
                return ResponseEntity.status(400).body(error);
            }
            String contentType = logo.getContentType() != null ? logo.getContentType() : "image/png";
            String base64 = Base64.getEncoder().encodeToString(logo.getBytes());
            settingsService.setSetting("company.logo", "data:" + contentType + ";base64," + base64, "company", userId);
        }

        auditLogService.logFromRequest(request, userId, AuditLog.ActionType.UPDATE_SETTINGS,
                "Mise à jour des informations entreprise", "Nom : " + name);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Informations entreprise enregistrées");
        response.put("logoUrl", settingsService.getSetting("company.logo", ""));
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/company", consumes = {"application/json"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> saveCompanyConfigJson(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        User user = getUserFromPrincipal(principal);
        Long userId = user.getId();
        settingsService.setSetting("company.name", body.getOrDefault("name", ""), "company", userId);

        auditLogService.logFromRequest(request, userId, AuditLog.ActionType.UPDATE_SETTINGS,
                "Mise à jour des informations entreprise", "Nom : " + body.getOrDefault("name", ""));

        Map<String, String> response = new HashMap<>();
        response.put("message", "Informations entreprise enregistrées");
        return ResponseEntity.ok(response);
    }

    // ==================== Destinataires Email ====================

    @GetMapping("/email-recipients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmailRecipient>> getEmailRecipients() {
        return ResponseEntity.ok(emailRecipientRepository.findAll());
    }

    @PostMapping("/email-recipients")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmailRecipient> addEmailRecipient(
            @RequestBody EmailRecipient recipient,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        User user = getUserFromPrincipal(principal);
        recipient.setId(null);
        EmailRecipient saved = emailRecipientRepository.save(recipient);

        auditLogService.logFromRequest(request, user.getId(), AuditLog.ActionType.UPDATE_SETTINGS,
                "Ajout d'un destinataire email", saved.getEmail());

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/email-recipients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> removeEmailRecipient(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            HttpServletRequest request
    ) {
        User user = getUserFromPrincipal(principal);
        emailRecipientRepository.deleteById(id);

        auditLogService.logFromRequest(request, user.getId(), AuditLog.ActionType.UPDATE_SETTINGS,
                "Suppression d'un destinataire email", "id=" + id);

        Map<String, Boolean> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
