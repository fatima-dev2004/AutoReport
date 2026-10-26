package com.sews.reporting.service;

import com.sews.reporting.entity.RefreshToken;
import com.sews.reporting.entity.User;
import com.sews.reporting.exception.TokenRefreshException;
import com.sews.reporting.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service de gestion des refresh tokens
 * Section améliorée : Authentification avec refresh token et déconnexion sécurisée
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration-ms:604800000}") // 7 jours par défaut
    private long refreshTokenDurationMs;

    /**
     * Crée un nouveau refresh token pour l'utilisateur
     */
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        // Révoquer tous les anciens tokens de l'utilisateur (une seule session active)
        // Commenter cette ligne pour permettre plusieurs sessions simultanées
        refreshTokenRepository.revokeAllUserTokens(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Trouve un refresh token valide par son token
     */
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenRefreshException("Refresh token introuvable"));
    }

    /**
     * Vérifie et retourne un refresh token valide
     */
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked()) {
            throw new TokenRefreshException("Refresh token révoqué");
        }

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException("Refresh token expiré. Veuillez vous reconnecter.");
        }

        return token;
    }

    /**
     * Révoque un refresh token
     */
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Révoque tous les refresh tokens d'un utilisateur
     */
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
    }

    /**
     * Supprime tous les refresh tokens expirés (tâche planifiée)
     * S'exécute tous les jours à 3h du matin
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.deleteExpiredTokens(now);
        log.info("Nettoyage des refresh tokens expirés effectué");
    }
}
