package com.sews.reporting.service;

import com.sews.reporting.entity.User;
import com.sews.reporting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service de gestion des photos de profil des utilisateurs
 * 
 * Fonctionnalités :
 * - Upload de photo de profil
 * - Validation du format et de la taille
 * - Sauvegarde sur le disque
 * - Suppression de photo
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPhotoService {

    private final UserRepository userRepository;

    @Value("${app.upload.photos-dir:profile-photos}")
    private String photosDirectory;

    // Formats autorisés
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    
    // Taille max : 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Sauvegarde la photo de profil d'un utilisateur
     */
    public String saveProfilePhoto(Long userId, MultipartFile file) throws IOException {
        log.info("Debut de l'upload de photo pour l'utilisateur {}", userId);

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        // Validation du fichier
        validatePhotoFile(file);

        // Supprimer l'ancienne photo si elle existe
        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
            deletePhotoFile(user.getProfilePhotoUrl());
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String newFilename = userId + "_" + UUID.randomUUID() + "." + extension;

        // Créer le dossier s'il n'existe pas
        Path uploadPath = Paths.get(photosDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Mettre à jour l'URL dans la base de données
        String photoUrl = "/api/users/photos/" + newFilename;
        user.setProfilePhotoUrl(photoUrl);
        userRepository.save(user);

        log.info("Photo de profil uploadee avec succes : {}", photoUrl);
        return photoUrl;
    }

    /**
     * Supprime la photo de profil d'un utilisateur
     */
    public void deleteProfilePhoto(Long userId) {
        log.info("Suppression de la photo pour l'utilisateur {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
            deletePhotoFile(user.getProfilePhotoUrl());
            user.setProfilePhotoUrl(null);
            userRepository.save(user);
            log.info("Photo de profil supprimee avec succes");
        }
    }

    /**
     * Valide le fichier uploadé
     */
    private void validatePhotoFile(MultipartFile file) {
        // Vérifier que le fichier n'est pas vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est vide");
        }

        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "La taille du fichier depasse la limite de 5 MB");
        }

        // Vérifier l'extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Nom de fichier invalide");
        }

        String extension = getFileExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Format de fichier non autorise. Formats acceptes : " + 
                    String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit etre une image");
        }
    }

    /**
     * Extrait l'extension du fichier
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("Le fichier n'a pas d'extension");
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * Supprime un fichier photo du disque
     */
    private void deletePhotoFile(String photoUrl) {
        try {
            // Extraire le nom du fichier de l'URL
            String filename = photoUrl.substring(photoUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(photosDirectory).resolve(filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Fichier photo supprime : {}", filename);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la suppression de la photo : {}", photoUrl, e);
        }
    }
}
