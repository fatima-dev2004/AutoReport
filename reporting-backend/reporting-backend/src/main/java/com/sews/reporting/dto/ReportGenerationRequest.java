package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationRequest {
    
    // Période du rapport
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Informations personnalisables
    private String reportTitle;           // Titre personnalisé du rapport
    private String generatedBy;           // Nom de la personne qui génère
    private String comments;              // Commentaires additionnels
    private String department;            // Département concerné
    private String reportPurpose;         // Objectif du rapport
    
    // Options d'affichage
    private Boolean includeCharts;        // Inclure les graphiques
    private Boolean includeCoverPage;     // Inclure la page de garde
    private Boolean includeDetailedData;  // Inclure les données détaillées
    
    // Type de rapport
    private String reportType;            // PRODUCTION, QUALITE, AUTRE
}
