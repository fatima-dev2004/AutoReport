package com.sews.reporting.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service de blacklist pour les tokens JWT révoqués
 * Utilise une Map en mémoire pour stocker les tokens révoqués
 * 
 * Note : Pour un environnement de production avec plusieurs instances,
 * il faudrait utiliser Redis pour une blacklist partagée
 */
@Service
public class JwtBlacklistService {

    // Map : token -> expiration time
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Ajoute un token à la blacklist
     */
    public void blacklistToken(String token, LocalDateTime expirationTime) {
        blacklistedTokens.put(token, expirationTime);
        cleanupExpiredTokens();
    }

    /**
     * Vérifie si un token est blacklisté
     */
    public boolean isBlacklisted(String token) {
        cleanupExpiredTokens();
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Nettoie les tokens expirés de la blacklist
     * (ils ne peuvent plus être utilisés de toute façon)
     */
    private void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    /**
     * Compte le nombre de tokens actuellement blacklistés
     */
    public int getBlacklistedTokensCount() {
        cleanupExpiredTokens();
        return blacklistedTokens.size();
    }

    /**
     * Vide complètement la blacklist (utile pour les tests)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }
}
