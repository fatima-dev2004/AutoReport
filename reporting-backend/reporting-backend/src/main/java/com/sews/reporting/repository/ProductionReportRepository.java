package com.sews.reporting.repository;

import com.sews.reporting.entity.ProductionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ProductionReportRepository extends JpaRepository<ProductionReport, Long> {

    List<ProductionReport> findByTypeOrderByGeneratedAtDesc(ProductionReport.ReportType type);

    List<ProductionReport> findAllByOrderByGeneratedAtDesc();
    
    // Filtrage par département (pour les employés USER)
    List<ProductionReport> findByDepartmentOrderByGeneratedAtDesc(String department);
    
    // Filtrage par département avec recherche
    List<ProductionReport> findByDepartmentAndTitleContainingIgnoreCaseOrderByGeneratedAtDesc(String department, String title);

    @Query("SELECT r FROM ProductionReport r WHERE r.periodStart >= :start AND r.periodEnd <= :end ORDER BY r.generatedAt DESC")
    List<ProductionReport> findByPeriod(LocalDate start, LocalDate end);

    List<ProductionReport> findByTitleContainingIgnoreCase(String title);
    
    // ============ Méthodes avec pagination (NOUVEAU) ============
    
    /**
     * Récupérer tous les rapports avec pagination
     */
    Page<ProductionReport> findAll(Pageable pageable);
    
    /**
     * Récupérer les rapports par département avec pagination
     */
    Page<ProductionReport> findByDepartment(String department, Pageable pageable);
    
    /**
     * Recherche par titre avec pagination
     */
    Page<ProductionReport> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Recherche par département et titre avec pagination
     */
    Page<ProductionReport> findByDepartmentAndTitleContainingIgnoreCase(
        String department, String title, Pageable pageable
    );
    
    // ============ AUTORISATION DES RAPPORTS POUR USER (CDC 5.1) ============
    
    /**
     * Récupère les rapports autorisés pour un utilisateur spécifique.
     * Un rapport est autorisé si:
     * - L'utilisateur l'a créé (generatedBy)
     * - Le rapport est de son département
     * - Il a une autorisation explicite (via ReportAuthorization)
     */
    @Query("SELECT DISTINCT r FROM ProductionReport r " +
           "LEFT JOIN ReportAuthorization ra ON ra.report.id = r.id " +
           "WHERE r.generatedBy.id = :userId " +
           "OR r.department = :department " +
           "OR ra.authorizedUser.id = :userId " +
           "ORDER BY r.generatedAt DESC")
    List<ProductionReport> findAuthorizedReportsForUser(
        @Param("userId") Long userId, 
        @Param("department") String department
    );
    
    /**
     * Version paginée des rapports autorisés
     */
    @Query("SELECT DISTINCT r FROM ProductionReport r " +
           "LEFT JOIN ReportAuthorization ra ON ra.report.id = r.id " +
           "WHERE r.generatedBy.id = :userId " +
           "OR r.department = :department " +
           "OR ra.authorizedUser.id = :userId")
    Page<ProductionReport> findAuthorizedReportsForUser(
        @Param("userId") Long userId, 
        @Param("department") String department,
        Pageable pageable
    );
    
    /**
     * Vérifie si un utilisateur a accès à un rapport spécifique
     */
    @Query("SELECT COUNT(r) > 0 FROM ProductionReport r " +
           "LEFT JOIN ReportAuthorization ra ON ra.report.id = r.id " +
           "WHERE r.id = :reportId AND (" +
           "r.generatedBy.id = :userId " +
           "OR r.department = :department " +
           "OR ra.authorizedUser.id = :userId)")
    boolean canUserAccessReport(
        @Param("reportId") Long reportId,
        @Param("userId") Long userId,
        @Param("department") String department
    );

    // ============ Options de filtres (page Reports du frontend) ============

    @Query("SELECT DISTINCT r.department FROM ProductionReport r WHERE r.department IS NOT NULL ORDER BY r.department")
    List<String> findDistinctDepartments();
}
