package com.sews.reporting.repository;

import com.sews.reporting.entity.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findByActiveTrue();
}
