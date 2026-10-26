package com.sews.reporting.controller;

import com.sews.reporting.dto.CreateUserRequest;
import com.sews.reporting.dto.UpdateUserRequest;
import com.sews.reporting.dto.UserPermissionsRequest;
import com.sews.reporting.entity.Permission;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.UserRepository;
import com.sews.reporting.service.AuthService;
import com.sews.reporting.service.UserPhotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Section 4 - Acteurs du systeme : Administrateur
 * - gerer les utilisateurs
 * - gerer les droits d'acces (NOUVEAU : permissions granulaires)
 * - gerer les photos de profil (NOUVEAU)
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserPhotoService userPhotoService;

    // ============ Gestion des Utilisateurs ============

    /**
     * Liste tous les utilisateurs avec pagination
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<User>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        org.springframework.data.domain.Sort.Direction direction = 
            sortDirection.equalsIgnoreCase("ASC") 
                ? org.springframework.data.domain.Sort.Direction.ASC 
                : org.springframework.data.domain.Sort.Direction.DESC;
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, direction, sortBy);
        
        org.springframework.data.domain.Page<User> users;
        
        if (search == null || search.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                search, search, pageable
            );
        }
        
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User created = authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, 
                                          @RequestBody UpdateUserRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    if (request.getFullName() != null) {
                        user.setFullName(request.getFullName());
                    }
                    if (request.getEmail() != null) {
                        user.setEmail(request.getEmail());
                    }
                    if (request.getDepartment() != null) {
                        user.setDepartment(request.getDepartment());
                    }
                    if (request.getPhoneNumber() != null) {
                        user.setPhoneNumber(request.getPhoneNumber());
                    }
                    if (request.getPosition() != null) {
                        user.setPosition(request.getPosition());
                    }
                    if (request.getRole() != null) {
                        user.setRole(request.getRole());
                    }
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setEnabled(false);
            userRepository.save(u);
        });
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setEnabled(true);
            userRepository.save(u);
        });
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, 
                                          @RequestParam com.sews.reporting.entity.Role role) {
        userRepository.findById(id).ifPresent(u -> {
            u.setRole(role);
            // Réinitialiser les permissions par défaut pour le nouveau rôle
            u.getPermissions().clear();
            u.assignDefaultPermissions();
            userRepository.save(u);
        });
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ============ Gestion des Photos de Profil (NOUVEAU) ============

    /**
     * Upload de la photo de profil
     */
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("photo") MultipartFile photo) {
        
        try {
            String photoUrl = userPhotoService.saveProfilePhoto(id, photo);
            return ResponseEntity.ok(Map.of(
                "message", "Photo uploadee avec succes",
                "photoUrl", photoUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Suppression de la photo de profil
     */
    @DeleteMapping("/{id}/photo")
    public ResponseEntity<Map<String, String>> deletePhoto(@PathVariable Long id) {
        try {
            userPhotoService.deleteProfilePhoto(id);
            return ResponseEntity.ok(Map.of("message", "Photo supprimee avec succes"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============ Gestion des Permissions (NOUVEAU) ============

    /**
     * Obtenir les permissions d'un utilisateur
     */
    @GetMapping("/{id}/permissions")
    public ResponseEntity<Set<Permission>> getUserPermissions(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(user.getPermissions()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Définir les permissions d'un utilisateur
     */
    @PutMapping("/{id}/permissions")
    public ResponseEntity<Map<String, String>> setUserPermissions(
            @PathVariable Long id,
            @RequestBody UserPermissionsRequest request) {
        
        return userRepository.findById(id)
                .map(user -> {
                    user.getPermissions().clear();
                    user.getPermissions().addAll(request.getPermissions());
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of(
                        "message", "Permissions mises a jour avec succes",
                        "permissionsCount", String.valueOf(user.getPermissions().size())
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Ajouter une permission à un utilisateur
     */
    @PostMapping("/{id}/permissions/{permission}")
    public ResponseEntity<Map<String, String>> addPermission(
            @PathVariable Long id,
            @PathVariable Permission permission) {
        
        return userRepository.findById(id)
                .map(user -> {
                    user.addPermission(permission);
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of(
                        "message", "Permission ajoutee avec succes",
                        "permission", permission.name()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retirer une permission d'un utilisateur
     */
    @DeleteMapping("/{id}/permissions/{permission}")
    public ResponseEntity<Map<String, String>> removePermission(
            @PathVariable Long id,
            @PathVariable Permission permission) {
        
        return userRepository.findById(id)
                .map(user -> {
                    user.removePermission(permission);
                    userRepository.save(user);
                    return ResponseEntity.ok(Map.of(
                        "message", "Permission retiree avec succes",
                        "permission", permission.name()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtenir toutes les permissions disponibles
     */
    @GetMapping("/permissions/available")
    public ResponseEntity<List<Map<String, String>>> getAvailablePermissions() {
        List<Map<String, String>> permissions = List.of(Permission.values()).stream()
                .map(p -> Map.of(
                    "name", p.name(),
                    "description", p.getDescription()
                ))
                .toList();
        return ResponseEntity.ok(permissions);
    }
}
