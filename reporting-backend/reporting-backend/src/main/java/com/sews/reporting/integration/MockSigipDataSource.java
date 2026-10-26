package com.sews.reporting.integration;

import com.sews.reporting.entity.ProductionRecord;
import com.sews.reporting.repository.ProductionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implémentation MOCK de la source de données SIGIP
 * 
 * Cette implémentation simule la connexion au système SIGIP (DB2 IBM i)
 * et génère des données de test réalistes.
 * 
 * Active en mode développement/démonstration (profile "dev" ou "demo")
 * 
 * Pour la production réelle, utiliser SigipDB2DataSource avec JDBC DB2
 */
@Service
@Profile({"dev", "demo", "default"})
@RequiredArgsConstructor
@Slf4j
public class MockSigipDataSource implements ExternalDataSource {

    private final ProductionRecordRepository productionRecordRepository;
    private final Random random = new Random();
    
    private static final String[] LINES = {"Ligne A", "Ligne B", "Ligne C", "Ligne D"};
    
    @Override
    public String getSourceName() {
        return "SIGIP Mock (Développement)";
    }

    @Override
    public String getSystemType() {
        return "SIGIP DB2 IBM i (Simulé)";
    }

    @Override
    public boolean testConnection() {
        log.info("Test de connexion SIGIP Mock - Simulation OK");
        // Simuler un léger délai réseau
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }

    @Override
    public List<ProductionRecord> fetchProductionData(LocalDate startDate, LocalDate endDate) 
            throws ExternalDataSourceException {
        
        log.info("Récupération des données SIGIP Mock de {} à {}", startDate, endDate);
        
        try {
            // Simuler un délai de réseau
            Thread.sleep(200);
            
            List<ProductionRecord> records = new ArrayList<>();
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            
            // Générer des données réalistes pour chaque jour et chaque ligne
            for (int day = 0; day < daysBetween; day++) {
                LocalDate currentDate = startDate.plusDays(day);
                
                for (String line : LINES) {
                    // Générer une production réaliste (avec variations)
                    double baseProduction = 800 + random.nextInt(400); // 800-1200 unités
                    
                    // Ajouter des variations par jour de la semaine
                    if (currentDate.getDayOfWeek().getValue() >= 6) {
                        baseProduction *= 0.7; // Week-end : production réduite
                    }
                    
                    // Simuler des événements exceptionnels (5% de chance)
                    if (random.nextDouble() < 0.05) {
                        baseProduction *= 0.5; // Problème de production
                    }
                    
                    // Calculer les défauts (normalement 2-5% de la production)
                    int defects = (int) (baseProduction * (0.02 + random.nextDouble() * 0.03));
                    
                    // Simuler des pics de défauts (3% de chance)
                    if (random.nextDouble() < 0.03) {
                        defects *= 3;
                    }
                    
                    ProductionRecord record = ProductionRecord.builder()
                            .productionDate(currentDate)
                            .line(line)
                            .quantity(baseProduction)
                            .defects(defects)
                            .build();
                    
                    records.add(record);
                }
            }
            
            log.info("{} enregistrements SIGIP Mock générés", records.size());
            return records;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalDataSourceException("Interruption lors de la récupération des données", e);
        }
    }

    @Override
    public int synchronizeData(LocalDate startDate, LocalDate endDate) 
            throws ExternalDataSourceException {
        
        log.info("Synchronisation des données SIGIP Mock de {} à {}", startDate, endDate);
        
        // Récupérer les données depuis la source "externe"
        List<ProductionRecord> externalRecords = fetchProductionData(startDate, endDate);
        
        // Supprimer les données existantes pour cette période (éviter les doublons)
        List<ProductionRecord> existingRecords = 
                productionRecordRepository.findByProductionDateBetween(startDate, endDate);
        
        if (!existingRecords.isEmpty()) {
            log.info("Suppression de {} enregistrements existants", existingRecords.size());
            productionRecordRepository.deleteAll(existingRecords);
        }
        
        // Sauvegarder les nouvelles données
        List<ProductionRecord> savedRecords = productionRecordRepository.saveAll(externalRecords);
        
        log.info("Synchronisation SIGIP Mock terminée : {} enregistrements importés", savedRecords.size());
        return savedRecords.size();
    }

    @Override
    public boolean isAvailable() {
        // Mock est toujours disponible
        return true;
    }

    @Override
    public ConnectionMetadata getConnectionMetadata() {
        return new ConnectionMetadata(
                "localhost (mock)",
                "SIGIP_DEV",
                "CONNECTED (Simulé)",
                java.time.LocalDateTime.now().toString(),
                true
        );
    }
}
