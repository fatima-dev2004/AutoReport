package com.sews.reporting.integration;

import com.sews.reporting.entity.ProductionRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface pour l'intégration avec des sources de données externes
 * 
 * Cette interface permet de rendre l'architecture extensible pour supporter
 * différents systèmes externes (SIGIP, ERP, MES, etc.)
 * 
 * Conforme CDC Section 8 : Évolution possible - Intégration SIGIP (DB2 IBM i)
 */
public interface ExternalDataSource {
    
    /**
     * Nom de la source de données
     */
    String getSourceName();
    
    /**
     * Type de système (SIGIP, SAP, ORACLE, etc.)
     */
    String getSystemType();
    
    /**
     * Teste la connexion à la source de données externe
     * 
     * @return true si la connexion est établie, false sinon
     */
    boolean testConnection();
    
    /**
     * Récupère les données de production depuis la source externe
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des enregistrements de production
     * @throws ExternalDataSourceException si une erreur survient
     */
    List<ProductionRecord> fetchProductionData(LocalDate startDate, LocalDate endDate) 
            throws ExternalDataSourceException;
    
    /**
     * Synchronise les données de production (import depuis la source externe vers la base locale)
     * 
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Nombre d'enregistrements synchronisés
     * @throws ExternalDataSourceException si une erreur survient
     */
    int synchronizeData(LocalDate startDate, LocalDate endDate) 
            throws ExternalDataSourceException;
    
    /**
     * Vérifie si la source est disponible et opérationnelle
     */
    boolean isAvailable();
    
    /**
     * Récupère les métadonnées de connexion (sans informations sensibles)
     */
    ConnectionMetadata getConnectionMetadata();
    
    /**
     * Métadonnées de connexion
     */
    class ConnectionMetadata {
        private String host;
        private String database;
        private String status;
        private String lastSync;
        private boolean enabled;
        
        public ConnectionMetadata(String host, String database, String status, String lastSync, boolean enabled) {
            this.host = host;
            this.database = database;
            this.status = status;
            this.lastSync = lastSync;
            this.enabled = enabled;
        }
        
        // Getters
        public String getHost() { return host; }
        public String getDatabase() { return database; }
        public String getStatus() { return status; }
        public String getLastSync() { return lastSync; }
        public boolean isEnabled() { return enabled; }
    }
}
