package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Une ligne de donnees issue du fichier Excel importe (section 5.2 / 5.3).
 * Colonnes attendues : Production_Date, Line, Quantity, Defects
 */
@Entity
@Table(name = "production_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_date", nullable = false)
    private LocalDate productionDate;

    @Column(name = "production_line", nullable = false)
    private String line;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Integer defects;

    @Column(name = "source_file_name")
    private String sourceFileName;
}
