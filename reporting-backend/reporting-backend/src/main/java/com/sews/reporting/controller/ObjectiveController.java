package com.sews.reporting.controller;

import com.sews.reporting.dto.ObjectiveDto;
import com.sews.reporting.entity.ProductionObjective;
import com.sews.reporting.repository.ProductionObjectiveRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/objectives")
@RequiredArgsConstructor
@Slf4j
public class ObjectiveController {
    
    private final ProductionObjectiveRepository objectiveRepository;
    
    /**
     * Liste des objectifs avec pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<Page<ObjectiveDto>> listObjectives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(required = false) String line,
            @RequestParam(required = false) Boolean active
    ) {
        log.info("Listing objectives - page: {}, size: {}, line: {}, active: {}", page, size, line, active);
        
        Sort sort = sortDirection.equalsIgnoreCase("ASC") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductionObjective> objectives;
        
        // Filtrage
        if (line != null && !line.isEmpty()) {
            objectives = objectiveRepository.findByLineAndActiveTrue(line, pageable);
        } else if (active != null && active) {
            objectives = objectiveRepository.findByActiveTrue(pageable);
        } else {
            objectives = objectiveRepository.findAll(pageable);
        }
        
        Page<ObjectiveDto> dtoPage = objectives.map(ObjectiveDto::new);
        
        return ResponseEntity.ok(dtoPage);
    }
    
    /**
     * Créer un nouvel objectif
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ObjectiveDto> createObjective(@RequestBody ProductionObjective objective) {
        log.info("Creating new objective for line: {}, period: {}, targetQty: {}, maxDefects: {}", 
                 objective.getLine(), objective.getPeriodType(), objective.getTargetQuantity(), objective.getMaxDefects());
        
        // Valeurs par défaut si null
        if (objective.getActive() == null) {
            objective.setActive(true);
        }
        if (objective.getMaxDefects() == null) {
            log.warn("maxDefects is null, setting to 0");
            objective.setMaxDefects(0);
        }
        if (objective.getPeriodType() == null) {
            log.warn("periodType is null, setting to MONTHLY");
            objective.setPeriodType(ProductionObjective.PeriodType.MONTHLY);
        }
        if (objective.getTargetQualityRate() == null) {
            log.warn("targetQualityRate is null, setting to 95.0");
            objective.setTargetQualityRate(95.0);
        }
        if (objective.getTargetQuantity() == null) {
            log.warn("targetQuantity is null, setting to 0");
            objective.setTargetQuantity(0);
        }
        
        log.info("Saving objective: {}", objective);
        ProductionObjective saved = objectiveRepository.save(objective);
        
        return ResponseEntity.ok(new ObjectiveDto(saved));
    }
    
    /**
     * Récupérer un objectif par ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ObjectiveDto> getObjective(@PathVariable Long id) {
        return objectiveRepository.findById(id)
            .map(obj -> ResponseEntity.ok(new ObjectiveDto(obj)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Mettre à jour un objectif
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ObjectiveDto> updateObjective(
            @PathVariable Long id,
            @RequestBody ProductionObjective objectiveUpdate
    ) {
        return objectiveRepository.findById(id)
            .map(existing -> {
                existing.setLine(objectiveUpdate.getLine());
                existing.setPeriodType(objectiveUpdate.getPeriodType());
                existing.setStartDate(objectiveUpdate.getStartDate());
                existing.setEndDate(objectiveUpdate.getEndDate());
                existing.setTargetQuantity(objectiveUpdate.getTargetQuantity());
                existing.setTargetQualityRate(objectiveUpdate.getTargetQualityRate());
                existing.setMaxDefects(objectiveUpdate.getMaxDefects());
                existing.setNotes(objectiveUpdate.getNotes());
                
                ProductionObjective saved = objectiveRepository.save(existing);
                return ResponseEntity.ok(new ObjectiveDto(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Désactiver (archiver) un objectif
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteObjective(@PathVariable Long id) {
        return objectiveRepository.findById(id)
            .map(objective -> {
                objective.setActive(false);
                objectiveRepository.save(objective);
                log.info("Objective {} archived", id);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Récupérer les objectifs pour une période donnée
     */
    @GetMapping("/period")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<List<ObjectiveDto>> getObjectivesByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String line
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        List<ProductionObjective> objectives;
        
        if (line != null && !line.isEmpty()) {
            objectives = objectiveRepository.findByLineAndPeriod(line, start, end);
        } else {
            objectives = objectiveRepository.findByPeriod(start, end);
        }
        
        List<ObjectiveDto> dtos = objectives.stream()
            .map(ObjectiveDto::new)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }
}
