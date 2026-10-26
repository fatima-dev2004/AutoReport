package com.sews.reporting.repository;

import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<AuditLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    Page<AuditLog> findByActionTypeOrderByTimestampDesc(AuditLog.ActionType actionType, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:userId IS NULL OR a.user.id = :userId) AND " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate) " +
            "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByFilters(
            @Param("userId") Long userId,
            @Param("actionType") AuditLog.ActionType actionType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    List<AuditLog> findTop10ByOrderByTimestampDesc();
}
