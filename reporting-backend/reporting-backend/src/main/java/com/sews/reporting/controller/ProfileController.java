package com.sews.reporting.controller;

import com.sews.reporting.entity.User;
import com.sews.reporting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Contrôleur pour la gestion du profil utilisateur courant
 * Permet à un utilisateur de consulter et modifier son propre profil
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    /**
     * Obtenir les informations du profil de l'utilisateur courant
     */
    @GetMapping
    public ResponseEntity<User> getCurrentUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findByEmail(auth.getName())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtenir l'URL de la photo de profil de l'utilisateur courant
     */
    @GetMapping("/photo-url")
    public ResponseEntity<Map<String, String>> getCurrentUserPhotoUrl() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findByEmail(auth.getName())
                .map(user -> {
                    String photoUrl = user.getProfilePhotoUrl();
                    if (photoUrl == null || photoUrl.isEmpty()) {
                        return ResponseEntity.ok(Map.of(
                            "photoUrl", "",
                            "hasPhoto", "false"
                        ));
                    }
                    return ResponseEntity.ok(Map.of(
                        "photoUrl", photoUrl,
                        "hasPhoto", "true"
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Mettre à jour les informations du profil (hors photo et email)
     */
    @PutMapping
    public ResponseEntity<User> updateProfile(@RequestBody UpdateProfileRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findByEmail(auth.getName())
                .map(user -> {
                    if (request.getFullName() != null && !request.getFullName().isBlank()) {
                        user.setFullName(request.getFullName());
                    }
                    if (request.getPhoneNumber() != null) {
                        user.setPhoneNumber(request.getPhoneNumber());
                    }
                    if (request.getPosition() != null) {
                        user.setPosition(request.getPosition());
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DTO pour la mise à jour du profil
    public static class UpdateProfileRequest {
        private String fullName;
        private String phoneNumber;
        private String position;

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
    }
}
