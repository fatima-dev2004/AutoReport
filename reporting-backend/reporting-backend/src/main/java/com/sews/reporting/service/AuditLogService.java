package com.sews.reporting.service;

import com.sews.reporting.dto.AuditLogDto;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.entity.User;
import com.sews.reporting.repository.AuditLogRepository;
import com.sews.reporting.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Async
    @Transactional
    public void log(Long userId, AuditLog.ActionType actionType, String action, String details) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        
        AuditLog log = AuditLog.builder()
                .user(user)
                .actionType(actionType)
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(log);
    }

    @Async
    @Transactional
    public void log(Long userId, AuditLog.ActionType actionType, String action, String details, 
                    String ipAddress, String userAgent) {
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        
        AuditLog log = AuditLog.builder()
                .user(user)
                .actionType(actionType)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .timestamp(LocalDateTime.now())
                .build();
        
        auditLogRepository.save(log);
    }

    @Async
    @Transactional
    public void logFromRequest(HttpServletRequest request, Long userId, 
                               AuditLog.ActionType actionType, String action, String details) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        log(userId, actionType, action, details, ipAddress, userAgent);
    }

    public Page<AuditLogDto> getAuditLogs(Long userId, AuditLog.ActionType actionType,
                                          LocalDateTime startDate, LocalDateTime endDate,
                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> logs = auditLogRepository.findByFilters(userId, actionType, startDate, endDate, pageable);
        return logs.map(this::convertToDto);
    }

    public List<AuditLogDto> getRecentLogs(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Regroupe le nombre d'evenements par categorie affichee dans le journal d'activite
     * (login / report / import / email / download), pour les cartes de statistiques.
     */
    public Map<String, Long> getStatsByCategory() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("login", 0L);
        stats.put("report", 0L);
        stats.put("import", 0L);
        stats.put("email", 0L);
        stats.put("download", 0L);
        stats.put("other", 0L);

        for (AuditLog log : auditLogRepository.findAll()) {
            String category = toCategory(log.getActionType());
            stats.merge(category, 1L, Long::sum);
        }
        return stats;
    }

    /**
     * Mappe une ActionType technique vers la categorie utilisee cote interface
     * (les filtres du journal d'activite : Connexions / Rapports / Imports / Emails / Telechargements).
     */
    public static String toCategory(AuditLog.ActionType actionType) {
        if (actionType == null) return "other";
        return switch (actionType) {
            case LOGIN, LOGOUT -> "login";
            case GENERATE_REPORT -> "report";
            case IMPORT_EXCEL -> "import";
            case SEND_EMAIL -> "email";
            case DOWNLOAD_REPORT -> "download";
            default -> "other";
        };
    }

    /**
     * Genere un export CSV des logs correspondant aux filtres donnes.
     */
    public byte[] exportToCsv(Long userId, AuditLog.ActionType actionType,
                               LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, 5000);
        Page<AuditLog> logs = auditLogRepository.findByFilters(userId, actionType, startDate, endDate, pageable);

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

        StringBuilder csv = new StringBuilder();
        csv.append("Date;Heure;Type;Utilisateur;Details;IP\n");
        for (AuditLog log : logs) {
            csv.append(log.getTimestamp().format(dateFmt)).append(';')
                    .append(log.getTimestamp().format(timeFmt)).append(';')
                    .append(toCategory(log.getActionType())).append(';')
                    .append(log.getUser() != null ? log.getUser().getEmail() : "Système").append(';')
                    .append(log.getDetails() != null ? log.getDetails().replace(";", ",").replace("\n", " ") : "").append(';')
                    .append(log.getIpAddress() != null ? log.getIpAddress() : "")
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private AuditLogDto convertToDto(AuditLog log) {
        return AuditLogDto.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .userName(log.getUser() != null ? log.getUser().getFullName() : "Système")
                .action(log.getAction())
                .actionType(log.getActionType())
                .timestamp(log.getTimestamp())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .build();
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
