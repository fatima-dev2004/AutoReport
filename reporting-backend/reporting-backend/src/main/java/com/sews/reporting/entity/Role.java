package com.sews.reporting.entity;

/**
 * Roles du systeme selon le cahier des charges :
 * - ADMIN : administrateur (gestion utilisateurs, droits, configuration)
 * - MANAGER : responsable (import Excel, generation rapports, statistiques)
 * - USER : employe / utilisateur simple (consultation des rapports autorises)
 */
public enum Role {
    ADMIN,
    MANAGER,
    USER
}
