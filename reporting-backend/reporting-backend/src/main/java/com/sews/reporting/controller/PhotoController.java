package com.sews.reporting.controller;

import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contrôleur SÉCURISÉ pour servir les photos de profil
 * CORRECTION : Accès restreint aux utilisateurs authentifiés uniquement
 */
@RestController
@RequestMapping("/api/users/photos")
@RequiredArgsConstructor
@Slf4j
public class PhotoController {

    @Value("${app.upload.photos-dir:profile-photos}")
    private String photosDirectory;
    
    private final AuditLogService auditLogService;

    /**
     * Récupère une photo de profil (SÉCURISÉ - authentification requise)
     * Les utilisateurs authentifiés peuvent voir les photos de tous les utilisateurs
     * (nécessaire pour afficher les avatars dans l'interface)
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getPhoto(
            @PathVariable String filename,
            HttpServletRequest request) {
        
        // Vérifier l'authentification (normalement déjà fait par Spring Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            log.warn("Tentative d'accès non autorisée à la photo : {}", filename);
            return ResponseEntity.status(401).build();
        }
        
        try {
            // Validation du nom de fichier pour éviter path traversal
            if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
                log.warn("Tentative de path traversal détectée : {}", filename);
                return ResponseEntity.badRequest().build();
            }
            
            Path filePath = Paths.get(photosDirectory).resolve(filename).normalize();
            
            // Vérifier que le fichier est bien dans le répertoire autorisé (sécurité supplémentaire)
            if (!filePath.startsWith(Paths.get(photosDirectory).toAbsolutePath())) {
                log.warn("Tentative d'accès à un fichier hors du répertoire autorisé : {}", filename);
                return ResponseEntity.badRequest().build();
            }
            
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Déterminer le type MIME
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                // Log de l'accès (optionnel, peut être désactivé si trop verbeux)
                log.debug("Photo servie : {} pour l'utilisateur : {}", filename, auth.getName());

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // Cache 1 heure
                        .body(resource);
            } else {
                log.warn("Photo introuvable ou non lisible : {}", filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la photo : {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}