package com.sews.reporting.repository;

import com.sews.reporting.entity.ProductionObjective;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionObjectiveRepository extends JpaRepository<ProductionObjective, Long> {
    
    // Trouver les objectifs actifs
    Page<ProductionObjective> findByActiveTrue(Pageable pageable);
    
    // Trouver par ligne
    Page<ProductionObjective> findByLineAndActiveTrue(String line, Pageable pageable);
    
    // Trouver par période
    @Query("SELECT o FROM ProductionObjective o WHERE o.active = true " +
           "AND o.startDate <= :endDate AND o.endDate >= :startDate")
    List<ProductionObjective> findByPeriod(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    // Trouver par ligne et période
    @Query("SELECT o FROM ProductionObjective o WHERE o.active = true " +
           "AND o.line = :line AND o.startDate <= :endDate AND o.endDate >= :startDate")
    List<ProductionObjective> findByLineAndPeriod(
        @Param("line") String line,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
