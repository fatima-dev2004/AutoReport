package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represente un rapport genere (production, qualite, etc.)
 * Correspond a la section 5.4 et 5.7 du cahier des charges.
 */
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private ReportType type;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    // Indicateurs calcules (section 5.3)
    private Double totalProduction;
    private Integer totalDefects;
    private Double qualityRate;

    // Detail par ligne de production, stocke en JSON simple (ligne:valeur;ligne:valeur)
    @Column(length = 2000)
    private String productionByLine;
    
    // Département ou ligne de production associé au rapport (pour filtrage par employé)
    private String department;

    // Chemins des fichiers generes
    private String pdfFilePath;
    private String excelFilePath;

    @ManyToOne
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    private LocalDateTime generatedAt;

    @Column(name = "source_file_name")
    private String sourceFileName;

    @PrePersist
    public void prePersist() {
        this.generatedAt = LocalDateTime.now();
    }

    public void setExcelPath(String s) {
        
    }

    public void setPdfPath(String s) {
    }

    public void setEndDate(LocalDate now) {
    }

    public void setStartDate(LocalDate localDate) {
    }

    public void setReportType(ReportType reportType) {
    }

    public void setReportName(String testReport) {
    }

    public enum ReportType {
        PRODUCTION,
        QUALITE,
        AUTRE
    }
}
