package com.sews.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Historique persistant des imports Excel (cote backend, centralise pour l'administrateur).
 *
 * Remplace/complete l'historique conserve cote frontend dans localStorage, qui est perdu
 * si le navigateur est vide, propre a chaque utilisateur/poste et non consultable par
 * l'administrateur.
 */
@Entity
@Table(name = "import_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "rows_total", nullable = false)
    private int rowsTotal;

    @Column(name = "rows_imported", nullable = false)
    private int rowsImported;

    @Column(name = "rows_skipped", nullable = false)
    private int rowsSkipped;

    @Column(name = "error_count", nullable = false)
    private int errorCount;

    // Erreurs detaillees serialisees (une par ligne : "ligne X: message")
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @PrePersist
    public void prePersist() {
        if (importDate == null) {
            importDate = LocalDateTime.now();
        }
    }

    public enum Status {
        SUCCESS,
        PARTIAL,
        FAILED
    }
}
