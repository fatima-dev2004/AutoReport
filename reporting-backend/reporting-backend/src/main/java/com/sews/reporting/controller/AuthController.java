package com.sews.reporting.controller;

import com.sews.reporting.dto.AuthResponse;
import com.sews.reporting.dto.LoginRequest;
import com.sews.reporting.dto.LogoutRequest;
import com.sews.reporting.dto.RefreshTokenRequest;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.exception.TokenRefreshException;
import com.sews.reporting.security.JwtBlacklistService;
import com.sews.reporting.security.JwtUtil;
import com.sews.reporting.service.AuditLogService;
import com.sews.reporting.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final JwtBlacklistService jwtBlacklistService;
    private final JwtUtil jwtUtil;

    /**
     * Section 5.1 : connexion sécurisée avec refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {
        AuthResponse response = authService.login(request, httpRequest);
        
        // Log the login action
        auditLogService.logFromRequest(
            httpRequest,
            response.getUserId(),
            AuditLog.ActionType.LOGIN,
            "Connexion réussie",
            "Utilisateur: " + request.getEmail()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Renouvellement du token d'accès via refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (TokenRefreshException e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Refresh token invalide ou expiré", e.getMessage()));
        }
    }

    /**
     * Déconnexion sécurisée avec révocation des tokens
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody(required = false) LogoutRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest
    ) {
        try {
            // Extraire le token JWT de l'en-tête Authorization
            String accessToken = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
                
                // Ajouter le token à la blacklist
                LocalDateTime expiration = convertToLocalDateTime(jwtUtil.extractExpiration(accessToken));
                jwtBlacklistService.blacklistToken(accessToken, expiration);
            }

            // Révoquer le refresh token
            String refreshToken = request != null ? request.getRefreshToken() : null;
            authService.logout(refreshToken, accessToken);

            // Log de déconnexion
            if (userDetails != null) {
                auditLogService.logFromRequest(
                    httpRequest,
                    null, // L'ID utilisateur sera résolu depuis userDetails
                    AuditLog.ActionType.LOGOUT,
                    "Déconnexion réussie",
                    "Utilisateur: " + userDetails.getUsername()
                );
            }

            return ResponseEntity.ok(new MessageResponse("Déconnexion réussie"));
        } catch (Exception e) {
            return ResponseEntity.ok(new MessageResponse("Déconnexion effectuée"));
        }
    }

    private LocalDateTime convertToLocalDateTime(java.util.Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    // Classes internes pour les réponses
    record MessageResponse(String message) {}
    record ErrorResponse(String error, String details) {}
}
