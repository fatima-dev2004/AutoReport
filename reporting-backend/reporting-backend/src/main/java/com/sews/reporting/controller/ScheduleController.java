package com.sews.reporting.controller;

import com.sews.reporting.entity.ReportSchedule;
import com.sews.reporting.repository.ReportScheduleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Section 5.8 : Planification des rapports (reserve a l'administrateur)
 * L'administrateur configure : frequence d'envoi, destinataires, type de rapport.
 */
@RestController
@RequestMapping("/api/admin/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ReportScheduleRepository reportScheduleRepository;

    @GetMapping
    public ResponseEntity<List<ReportSchedule>> list() {
        return ResponseEntity.ok(reportScheduleRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<ReportSchedule> create(@Valid @RequestBody ReportSchedule schedule) {
        return ResponseEntity.ok(reportScheduleRepository.save(schedule));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportSchedule> update(@PathVariable Long id, @RequestBody ReportSchedule updated) {
        ReportSchedule schedule = reportScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Planification introuvable"));

        schedule.setReportType(updated.getReportType());
        schedule.setCronExpression(updated.getCronExpression());
        schedule.setRecipients(updated.getRecipients());
        schedule.setActive(updated.isActive());

        return ResponseEntity.ok(reportScheduleRepository.save(schedule));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reportScheduleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
