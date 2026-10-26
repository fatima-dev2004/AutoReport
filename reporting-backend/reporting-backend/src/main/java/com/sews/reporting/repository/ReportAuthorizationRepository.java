package com.sews.reporting.repository;

import com.sews.reporting.entity.ReportAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportAuthorizationRepository extends JpaRepository<ReportAuthorization, Long> {

    /**
     * Vérifie si un utilisateur a l'autorisation d'accéder à un rapport
     */
    @Query("SELECT COUNT(ra) > 0 FROM ReportAuthorization ra " +
           "WHERE ra.report.id = :reportId AND ra.authorizedUser.id = :userId")
    boolean isUserAuthorizedForReport(@Param("reportId") Long reportId, @Param("userId") Long userId);

    /**
     * Récupère toutes les autorisations pour un rapport
     */
    List<ReportAuthorization> findByReportId(Long reportId);

    /**
     * Récupère toutes les autorisations d'un utilisateur
     */
    List<ReportAuthorization> findByAuthorizedUserId(Long userId);

    /**
     * Récupère une autorisation spécifique
     */
    Optional<ReportAuthorization> findByReportIdAndAuthorizedUserId(Long reportId, Long userId);

    /**
     * Supprime toutes les autorisations pour un rapport
     */
    void deleteByReportId(Long reportId);

    /**
     * Supprime une autorisation spécifique
     */
    void deleteByReportIdAndAuthorizedUserId(Long reportId, Long userId);
}
