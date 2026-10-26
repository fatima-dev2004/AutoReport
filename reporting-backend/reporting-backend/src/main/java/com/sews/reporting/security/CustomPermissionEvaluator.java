package com.sews.reporting.security;

import com.sews.reporting.entity.Permission;
import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.Role;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.ProductionReportRepository;
import com.sews.reporting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Évaluateur de permissions personnalisé pour vérifier les accès basés sur les Permission enum
 * 
 * Permet d'utiliser @PreAuthorize("hasPermission(#id, 'ProductionReport', 'VIEW')")
 * au lieu de simplement hasRole()
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserRepository userRepository;
    private final ProductionReportRepository reportRepository;

    /**
     * Évalue si l'utilisateur a la permission demandée sur une ressource
     * 
     * @param authentication Authentication de l'utilisateur
     * @param targetDomainObject Objet cible (ex: ProductionReport)
     * @param permission Permission demandée (ex: "VIEW", "DELETE")
     * @return true si l'utilisateur a la permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Récupérer l'utilisateur
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return false;
        }

        // ADMIN a toutes les permissions
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        // Vérifier la permission spécifique
        String permissionStr = permission.toString();
        
        return checkPermission(user, targetDomainObject, permissionStr);
    }

    /**
     * Évalue si l'utilisateur a la permission demandée sur une ressource identifiée par son ID
     * 
     * @param authentication Authentication de l'utilisateur
     * @param targetId ID de la ressource cible
     * @param targetType Type de la ressource (ex: "ProductionReport")
     * @param permission Permission demandée
     * @return true si l'utilisateur a la permission
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Récupérer l'utilisateur
        User user = getUserFromAuthentication(authentication);
        if (user == null) {
            return false;
        }

        // ADMIN a toutes les permissions
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        String permissionStr = permission.toString();

        // Charger l'objet selon le type
        if ("ProductionReport".equals(targetType)) {
            Long reportId = (Long) targetId;
            return checkReportPermission(user, reportId, permissionStr);
        }

        // Par défaut, refuser
        return false;
    }

    /**
     * Vérifie une permission générique
     */
    private boolean checkPermission(User user, Object targetObject, String permissionStr) {
        if (targetObject instanceof ProductionReport) {
            ProductionReport report = (ProductionReport) targetObject;
            return checkReportPermission(user, report.getId(), permissionStr);
        }

        // Par défaut, vérifier simplement si l'utilisateur a la permission dans son profil
        return hasUserPermission(user, permissionStr);
    }

    /**
     * Vérifie une permission sur un rapport
     */
    private boolean checkReportPermission(User user, Long reportId, String permissionStr) {
        // Vérifier d'abord si l'utilisateur a la permission de base
        if (!hasUserPermission(user, permissionStr)) {
            log.debug("User {} doesn't have permission {} in their profile", user.getEmail(), permissionStr);
            return false;
        }

        // Pour VIEW et DOWNLOAD, vérifier l'accès au rapport spécifique
        if ("VIEW".equals(permissionStr) || "DOWNLOAD".equals(permissionStr)) {
            // MANAGER voit tous les rapports
            if (user.getRole() == Role.MANAGER) {
                return true;
            }

            // USER doit avoir une autorisation spécifique
            if (user.getRole() == Role.USER) {
                String userDepartment = user.getDepartment() != null ? user.getDepartment() : "";
                boolean hasAccess = reportRepository.canUserAccessReport(reportId, user.getId(), userDepartment);
                
                if (!hasAccess) {
                    log.debug("User {} doesn't have access to report {}", user.getEmail(), reportId);
                }
                
                return hasAccess;
            }
        }

        // Pour les autres permissions (DELETE, GENERATE, etc.), vérifier selon le rôle
        if ("DELETE".equals(permissionStr)) {
            // Seuls ADMIN et MANAGER peuvent supprimer
            if (user.getRole() == Role.MANAGER) {
                // MANAGER peut supprimer ses propres rapports ou ceux de son département
                ProductionReport report = reportRepository.findById(reportId).orElse(null);
                if (report == null) {
                    return false;
                }
                
                boolean isCreator = report.getGeneratedBy() != null && 
                                   report.getGeneratedBy().getId().equals(user.getId());
                boolean sameDepartment = user.getDepartment() != null && 
                                        user.getDepartment().equals(report.getDepartment());
                
                return isCreator || sameDepartment;
            }
            return false;
        }

        return true;
    }

    /**
     * Vérifie si l'utilisateur a une permission dans son profil
     */
    private boolean hasUserPermission(User user, String permissionStr) {
        try {
            Permission permission = Permission.valueOf(permissionStr);
            return user.hasPermission(permission);
        } catch (IllegalArgumentException e) {
            log.warn("Permission inconnue: {}", permissionStr);
            return false;
        }
    }

    /**
     * Récupère l'utilisateur depuis l'authentification
     */
    private User getUserFromAuthentication(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }
}
