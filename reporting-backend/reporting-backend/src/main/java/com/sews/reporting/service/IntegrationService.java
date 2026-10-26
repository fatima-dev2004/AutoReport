package com.sews.reporting.service;

import com.sews.reporting.integration.ExternalDataSource;
import com.sews.reporting.integration.ExternalDataSourceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Service de gestion des intégrations avec les systèmes externes
 * Permet de centraliser l'accès aux différentes sources de données
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntegrationService {

    // Spring injecte automatiquement l'implémentation active (Mock ou DB2 selon le profil)
    private final List<ExternalDataSource> dataSources;

    /**
     * Récupère la source de données active
     */
    public ExternalDataSource getActiveDataSource() {
        if (dataSources.isEmpty()) {
            throw new RuntimeException("Aucune source de données externe configurée");
        }
        
        // Retourner la première source disponible
        ExternalDataSource activeSource = dataSources.stream()
                .filter(ExternalDataSource::isAvailable)
                .findFirst()
                .orElse(dataSources.get(0));
        
        log.info("Source de données active : {}", activeSource.getSourceName());
        return activeSource;
    }

    /**
     * Liste toutes les sources de données configurées
     */
    public List<ExternalDataSource> getAllDataSources() {
        return dataSources;
    }

    /**
     * Synchronise les données depuis la source externe
     */
    public int synchronizeFromExternal(LocalDate startDate, LocalDate endDate) 
            throws ExternalDataSourceException {
        
        ExternalDataSource source = getActiveDataSource();
        log.info("Synchronisation depuis {} : {} à {}", 
                source.getSourceName(), startDate, endDate);
        
        return source.synchronizeData(startDate, endDate);
    }

    /**
     * Teste la connexion à une source de données
     */
    public boolean testConnection(String sourceName) {
        return dataSources.stream()
                .filter(ds -> ds.getSourceName().equals(sourceName))
                .findFirst()
                .map(ExternalDataSource::testConnection)
                .orElse(false);
    }
}
