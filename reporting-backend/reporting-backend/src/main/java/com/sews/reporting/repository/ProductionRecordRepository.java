package com.sews.reporting.repository;

import com.sews.reporting.entity.ProductionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProductionRecordRepository extends JpaRepository<ProductionRecord, Long> {
    List<ProductionRecord> findByProductionDateBetween(LocalDate start, LocalDate end);
}
