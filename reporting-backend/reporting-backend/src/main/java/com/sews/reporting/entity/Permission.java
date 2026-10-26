package com.sews.reporting.entity;

/**
 * Permissions granulaires pour le contrôle d'accès fin
 * 
 * Chaque utilisateur peut avoir un ensemble de permissions
 * indépendamment de son rôle (pour plus de flexibilité)
 */
public enum Permission {
    
    // === Gestion des Utilisateurs (ADMIN uniquement par défaut) ===
    CREATE_USERS("Créer des utilisateurs"),
    VIEW_USERS("Voir les utilisateurs"),
    EDIT_USERS("Modifier les utilisateurs"),
    DELETE_USERS("Supprimer les utilisateurs"),
    MANAGE_PERMISSIONS("Gérer les permissions des utilisateurs"),
    
    // === Import Excel ===
    IMPORT_EXCEL("Importer des fichiers Excel"),
    VIEW_IMPORT_HISTORY("Voir l'historique des imports"),
    DELETE_IMPORT_DATA("Supprimer des données importées"),
    
    // === Génération de Rapports ===
    GENERATE_REPORTS("Générer des rapports"),
    VIEW_REPORTS("Consulter les rapports"),
    DOWNLOAD_REPORTS("Télécharger les rapports (PDF/Excel)"),
    DELETE_REPORTS("Supprimer des rapports"),
    SHARE_REPORTS("Partager des rapports par email"),
    
    // === Dashboard et KPI ===
    VIEW_DASHBOARD("Voir le tableau de bord"),
    EXPORT_DASHBOARD("Exporter les données du dashboard"),
    
    // === Planification ===
    CREATE_SCHEDULES("Créer des planifications de rapports"),
    VIEW_SCHEDULES("Voir les planifications"),
    EDIT_SCHEDULES("Modifier les planifications"),
    DELETE_SCHEDULES("Supprimer les planifications"),
    
    // === Configuration Système ===
    CONFIGURE_SYSTEM("Configurer les paramètres système"),
    VIEW_LOGS("Voir les logs système"),
    MANAGE_EMAIL_SETTINGS("Gérer les paramètres email");
    
    private final String description;
    
    Permission(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
