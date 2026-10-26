package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    // Photo de profil (stockée en base64 ou chemin fichier)
    @Column(length = 500)
    private String profilePhotoUrl;

    // Permissions granulaires
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    // Métadonnées
    private String department;  // Département
    private String phoneNumber; // Numéro de téléphone
    private String position;    // Poste

    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        // Attribution automatique des permissions selon le rôle
        if (permissions.isEmpty()) {
            assignDefaultPermissions();
        }
    }

    /**
     * Attribue les permissions par défaut selon le rôle
     */
    public void assignDefaultPermissions() {
        switch (role) {
            case ADMIN:
                // Admin a toutes les permissions
                permissions.addAll(Set.of(Permission.values()));
                break;
            case MANAGER:
                // Manager peut tout faire sauf gestion utilisateurs
                permissions.add(Permission.IMPORT_EXCEL);
                permissions.add(Permission.GENERATE_REPORTS);
                permissions.add(Permission.VIEW_REPORTS);
                permissions.add(Permission.DOWNLOAD_REPORTS);
                permissions.add(Permission.VIEW_DASHBOARD);
                permissions.add(Permission.DELETE_REPORTS);
                break;
            case USER:
                // User peut uniquement consulter
                permissions.add(Permission.VIEW_REPORTS);
                permissions.add(Permission.VIEW_DASHBOARD);
                break;
        }
    }

    /**
     * Vérifie si l'utilisateur a une permission spécifique
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    /**
     * Ajoute une permission
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    /**
     * Retire une permission
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }
}
