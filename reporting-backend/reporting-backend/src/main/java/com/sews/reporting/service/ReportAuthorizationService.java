package com.sews.reporting.service;

import com.sews.reporting.dto.ReportAuthorizationDto;
import com.sews.reporting.entity.ProductionReport;
import com.sews.reporting.entity.ReportAuthorization;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.ProductionReportRepository;
import com.sews.reporting.repository.ReportAuthorizationRepository;
import com.sews.reporting.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service de gestion des autorisations d'accès aux rapports
 * Implémente le contrôle d'accès fin requis par le CDC Section 5.1
 */
@Service
@RequiredArgsConstructor
public class ReportAuthorizationService {

    private final ReportAuthorizationRepository authorizationRepository;
    private final ProductionReportRepository reportRepository;
    private final UserRepository userRepository;

    /**
     * Autorise un utilisateur à accéder à un rapport
     */
    @Transactional
    public ReportAuthorizationDto authorizeUserForReport(Long reportId, Long userId, Long authorizedByUserId, String reason) {
        ProductionReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Rapport non trouvé: " + reportId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
        
        User authorizedBy = userRepository.findById(authorizedByUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur autorisateur non trouvé: " + authorizedByUserId));

        // Vérifier si l'autorisation existe déjà
        if (authorizationRepository.isUserAuthorizedForReport(reportId, userId)) {
            throw new RuntimeException("L'utilisateur a déjà accès à ce rapport");
        }

        ReportAuthorization authorization = ReportAuthorization.builder()
                .report(report)
                .authorizedUser(user)
                .authorizedBy(authorizedBy)
                .authorizationReason(reason)
                .build();

        authorization = authorizationRepository.save(authorization);
        return toDto(authorization);
    }

    /**
     * Révoque l'autorisation d'un utilisateur pour un rapport
     */
    @Transactional
    public void revokeUserAuthorization(Long reportId, Long userId) {
        authorizationRepository.deleteByReportIdAndAuthorizedUserId(reportId, userId);
    }

    /**
     * Récupère toutes les autorisations pour un rapport
     */
    public List<ReportAuthorizationDto> getAuthorizationsForReport(Long reportId) {
        return authorizationRepository.findByReportId(reportId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les autorisations d'un utilisateur
     */
    public List<ReportAuthorizationDto> getAuthorizationsForUser(Long userId) {
        return authorizationRepository.findByAuthorizedUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Vérifie si un utilisateur a l'autorisation d'accéder à un rapport
     */
    public boolean isUserAuthorized(Long reportId, Long userId) {
        return authorizationRepository.isUserAuthorizedForReport(reportId, userId);
    }

    /**
     * Autorise plusieurs utilisateurs en une seule fois
     */
    @Transactional
    public List<ReportAuthorizationDto> authorizeMultipleUsers(Long reportId, List<Long> userIds, 
                                                                 Long authorizedByUserId, String reason) {
        return userIds.stream()
                .map(userId -> authorizeUserForReport(reportId, userId, authorizedByUserId, reason))
                .collect(Collectors.toList());
    }

    /**
     * Conversion entité -> DTO
     */
    private ReportAuthorizationDto toDto(ReportAuthorization authorization) {
        return ReportAuthorizationDto.builder()
                .id(authorization.getId())
                .reportId(authorization.getReport().getId())
                .reportTitle(authorization.getReport().getTitle())
                .userId(authorization.getAuthorizedUser().getId())
                .userName(authorization.getAuthorizedUser().getFullName())
                .userEmail(authorization.getAuthorizedUser().getEmail())
                .authorizedByUserId(authorization.getAuthorizedBy() != null 
                        ? authorization.getAuthorizedBy().getId() : null)
                .authorizedByUserName(authorization.getAuthorizedBy() != null 
                        ? authorization.getAuthorizedBy().getFullName() : null)
                .authorizedAt(authorization.getAuthorizedAt())
                .authorizationReason(authorization.getAuthorizationReason())
                .build();
    }


}
