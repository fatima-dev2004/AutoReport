# Exigences Backend pour les Objectifs de Production

Ce document détaille l'implémentation complète de la fonctionnalité "Objectifs de Production" côté backend Spring Boot.

## Vue d'Ensemble

La fonctionnalité permet de :
- ✅ Définir des objectifs de production par ligne et par période
- ✅ Suivre la réalisation des objectifs en temps réel
- ✅ Comparer objectifs vs réalité
- ✅ Activer/désactiver des objectifs
- ✅ Gérer des objectifs avec dates de début/fin

## 1. Entité ProductionObjective

```java
package com.autoreport.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_objectives")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionObjective {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String line; // Ligne de production (ex: "Line A", "Line B")
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ObjectivePeriod period; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    @Column(nullable = false)
    private Integer targetQuantity; // Quantité à produire
    
    @Column(nullable = false)
    private Double targetQualityRate; // Taux de qualité cible (ex: 95.0 pour 95%)
    
    @Column(nullable = false)
    private Double maxDefectRate; // Taux de défaut maximum acceptable (ex: 5.0 pour 5%)
    
    @Column(nullable = false)
    private LocalDate startDate; // Date de début
    
    @Column
    private LocalDate endDate; // Date de fin (null = objectif permanent)
    
    @Column(nullable = false)
    private Boolean active = true; // Objectif actif ou non
    
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy; // Utilisateur qui a créé l'objectif
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

## 2. Enum ObjectivePeriod

```java
package com.autoreport.entity;

public enum ObjectivePeriod {
    DAILY,      // Objectif quotidien
    WEEKLY,     // Objectif hebdomadaire
    MONTHLY,    // Objectif mensuel
    YEARLY      // Objectif annuel
}
```

## 3. Repository ProductionObjectiveRepository

```java
package com.autoreport.repository;

import com.autoreport.entity.ProductionObjective;
import com.autoreport.entity.ObjectivePeriod;
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
    
    // Pagination simple
    Page<ProductionObjective> findAll(Pageable pageable);
    
    // Filtres avec pagination
    Page<ProductionObjective> findByLine(String line, Pageable pageable);
    Page<ProductionObjective> findByPeriod(ObjectivePeriod period, Pageable pageable);
    Page<ProductionObjective> findByActive(Boolean active, Pageable pageable);
    Page<ProductionObjective> findByLineAndActive(String line, Boolean active, Pageable pageable);
    
    // Trouver les objectifs actifs pour une ligne et une période donnée
    @Query("SELECT o FROM ProductionObjective o WHERE o.line = :line " +
           "AND o.active = true " +
           "AND o.startDate <= :date " +
           "AND (o.endDate IS NULL OR o.endDate >= :date)")
    List<ProductionObjective> findActiveObjectivesForLineAndDate(
        @Param("line") String line, 
        @Param("date") LocalDate date
    );
    
    // Récupérer les lignes de production distinctes
    @Query("SELECT DISTINCT o.line FROM ProductionObjective o ORDER BY o.line")
    List<String> findDistinctLines();
    
    // Vérifier les chevauchements d'objectifs
    @Query("SELECT COUNT(o) > 0 FROM ProductionObjective o WHERE o.line = :line " +
           "AND o.period = :period " +
           "AND o.active = true " +
           "AND o.id != :excludeId " +
           "AND o.startDate <= :endDate " +
           "AND (o.endDate IS NULL OR o.endDate >= :startDate)")
    boolean existsOverlappingObjective(
        @Param("line") String line,
        @Param("period") ObjectivePeriod period,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("excludeId") Long excludeId
    );
}
```

## 4. DTOs

### ObjectiveRequest (création/modification)

```java
package com.autoreport.dto;

import com.autoreport.entity.ObjectivePeriod;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
public class ObjectiveRequest {
    
    @NotBlank(message = "La ligne de production est requise")
    private String line;
    
    @NotNull(message = "La période est requise")
    private ObjectivePeriod period;
    
    @NotNull(message = "La quantité cible est requise")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer targetQuantity;
    
    @NotNull(message = "Le taux de qualité cible est requis")
    @DecimalMin(value = "0.0", message = "Le taux doit être entre 0 et 100")
    @DecimalMax(value = "100.0", message = "Le taux doit être entre 0 et 100")
    private Double targetQualityRate;
    
    @NotNull(message = "Le taux de défaut maximum est requis")
    @DecimalMin(value = "0.0", message = "Le taux doit être entre 0 et 100")
    @DecimalMax(value = "100.0", message = "Le taux doit être entre 0 et 100")
    private Double maxDefectRate;
    
    @NotNull(message = "La date de début est requise")
    private LocalDate startDate;
    
    private LocalDate endDate; // Optionnel
    
    private Boolean active = true;
}
```

### ObjectiveStatsResponse (statistiques)

```java
package com.autoreport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectiveStatsResponse {
    private Integer achieved;           // Quantité réalisée
    private Integer target;             // Quantité cible
    private Double rate;                // Taux de réalisation (%)
    private String status;              // ACHIEVED, IN_PROGRESS, NOT_ACHIEVED
    private Double currentQualityRate;  // Taux de qualité actuel
    private Double currentDefectRate;   // Taux de défaut actuel
}
```

### ComparisonResponse (comparaison objectifs vs réalité)

```java
package com.autoreport.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComparisonResponse {
    private String line;
    private Integer totalProduced;
    private Integer totalObjective;
    private Double achievementRate;
    private Double averageQualityRate;
    private List<ObjectiveComparison> objectives;
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ObjectiveComparison {
        private Long objectiveId;
        private String period;
        private Integer target;
        private Integer achieved;
        private Double rate;
        private String status;
    }
}
```

## 5. Service ProductionObjectiveService

```java
package com.autoreport.service;

import com.autoreport.dto.*;
import com.autoreport.entity.*;
import com.autoreport.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductionObjectiveService {
    
    @Autowired
    private ProductionObjectiveRepository objectiveRepository;
    
    @Autowired
    private ProductionDataRepository productionDataRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Lister les objectifs avec pagination et filtres
     */
    public Page<ProductionObjective> listObjectives(
        Integer page, Integer size, String line, 
        ObjectivePeriod period, Boolean active
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        
        if (line != null && period != null && active != null) {
            // Filtre combiné (créer une méthode dans le repo si nécessaire)
            return objectiveRepository.findByLineAndActive(line, active, pageable);
        } else if (line != null) {
            return objectiveRepository.findByLine(line, pageable);
        } else if (period != null) {
            return objectiveRepository.findByPeriod(period, pageable);
        } else if (active != null) {
            return objectiveRepository.findByActive(active, pageable);
        }
        
        return objectiveRepository.findAll(pageable);
    }
    
    /**
     * Créer un objectif
     */
    public ProductionObjective createObjective(ObjectiveRequest request, String userEmail) {
        // Vérifier les chevauchements
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.of(2099, 12, 31);
        boolean overlaps = objectiveRepository.existsOverlappingObjective(
            request.getLine(),
            request.getPeriod(),
            request.getStartDate(),
            endDate,
            -1L // Pas d'exclusion pour création
        );
        
        if (overlaps) {
            throw new IllegalArgumentException(
                "Un objectif actif existe déjà pour cette ligne et période aux dates spécifiées"
            );
        }
        
        ProductionObjective objective = new ProductionObjective();
        objective.setLine(request.getLine());
        objective.setPeriod(request.getPeriod());
        objective.setTargetQuantity(request.getTargetQuantity());
        objective.setTargetQualityRate(request.getTargetQualityRate());
        objective.setMaxDefectRate(request.getMaxDefectRate());
        objective.setStartDate(request.getStartDate());
        objective.setEndDate(request.getEndDate());
        objective.setActive(request.getActive());
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        objective.setCreatedBy(user);
        
        return objectiveRepository.save(objective);
    }
    
    /**
     * Mettre à jour un objectif
     */
    public ProductionObjective updateObjective(Long id, ObjectiveRequest request) {
        ProductionObjective objective = objectiveRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Objective not found"));
        
        // Vérifier les chevauchements (en excluant l'objectif actuel)
        LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.of(2099, 12, 31);
        boolean overlaps = objectiveRepository.existsOverlappingObjective(
            request.getLine(),
            request.getPeriod(),
            request.getStartDate(),
            endDate,
            id
        );
        
        if (overlaps) {
            throw new IllegalArgumentException(
                "Un objectif actif existe déjà pour cette ligne et période aux dates spécifiées"
            );
        }
        
        objective.setLine(request.getLine());
        objective.setPeriod(request.getPeriod());
        objective.setTargetQuantity(request.getTargetQuantity());
        objective.setTargetQualityRate(request.getTargetQualityRate());
        objective.setMaxDefectRate(request.getMaxDefectRate());
        objective.setStartDate(request.getStartDate());
        objective.setEndDate(request.getEndDate());
        objective.setActive(request.getActive());
        
        return objectiveRepository.save(objective);
    }
    
    /**
     * Supprimer un objectif
     */
    public void deleteObjective(Long id) {
        objectiveRepository.deleteById(id);
    }
    
    /**
     * Activer/désactiver un objectif
     */
    public ProductionObjective toggleStatus(Long id, Boolean active) {
        ProductionObjective objective = objectiveRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Objective not found"));
        objective.setActive(active);
        return objectiveRepository.save(objective);
    }
    
    /**
     * Récupérer les lignes de production disponibles
     */
    public List<String> getProductionLines() {
        return objectiveRepository.findDistinctLines();
    }
    
    /**
     * Calculer les statistiques d'un objectif
     */
    public ObjectiveStatsResponse getObjectiveStats(Long id) {
        ProductionObjective objective = objectiveRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Objective not found"));
        
        LocalDate endDate = objective.getEndDate() != null ? objective.getEndDate() : LocalDate.now();
        
        // Récupérer les données de production pour cette ligne et période
        List<ProductionData> productionData = productionDataRepository
            .findByLineAndDateBetween(objective.getLine(), objective.getStartDate(), endDate);
        
        int totalProduced = productionData.stream()
            .mapToInt(ProductionData::getQuantity)
            .sum();
        
        int totalDefects = productionData.stream()
            .mapToInt(ProductionData::getDefects)
            .sum();
        
        double qualityRate = totalProduced > 0 
            ? ((totalProduced - totalDefects) * 100.0 / totalProduced) 
            : 0.0;
        
        double defectRate = totalProduced > 0 
            ? (totalDefects * 100.0 / totalProduced) 
            : 0.0;
        
        double achievementRate = objective.getTargetQuantity() > 0
            ? (totalProduced * 100.0 / objective.getTargetQuantity())
            : 0.0;
        
        String status;
        if (achievementRate >= 100.0 && qualityRate >= objective.getTargetQualityRate()) {
            status = "ACHIEVED";
        } else if (LocalDate.now().isAfter(endDate)) {
            status = "NOT_ACHIEVED";
        } else {
            status = "IN_PROGRESS";
        }
        
        return new ObjectiveStatsResponse(
            totalProduced,
            objective.getTargetQuantity(),
            Math.round(achievementRate * 10.0) / 10.0,
            status,
            Math.round(qualityRate * 10.0) / 10.0,
            Math.round(defectRate * 10.0) / 10.0
        );
    }
    
    /**
     * Comparer objectifs vs réalité pour une période
     */
    public List<ComparisonResponse> compareObjectivesVsReality(
        LocalDate startDate, LocalDate endDate, String line
    ) {
        List<ProductionObjective> objectives;
        
        if (line != null) {
            objectives = objectiveRepository.findActiveObjectivesForLineAndDate(line, startDate);
        } else {
            objectives = objectiveRepository.findAll().stream()
                .filter(o -> o.getActive() && 
                       !o.getStartDate().isAfter(endDate) &&
                       (o.getEndDate() == null || !o.getEndDate().isBefore(startDate)))
                .collect(Collectors.toList());
        }
        
        // Grouper par ligne
        Map<String, List<ProductionObjective>> objectivesByLine = objectives.stream()
            .collect(Collectors.groupingBy(ProductionObjective::getLine));
        
        return objectivesByLine.entrySet().stream()
            .map(entry -> {
                String currentLine = entry.getKey();
                List<ProductionObjective> lineObjectives = entry.getValue();
                
                // Récupérer données de production
                List<ProductionData> productionData = productionDataRepository
                    .findByLineAndDateBetween(currentLine, startDate, endDate);
                
                int totalProduced = productionData.stream()
                    .mapToInt(ProductionData::getQuantity)
                    .sum();
                
                int totalObjective = lineObjectives.stream()
                    .mapToInt(ProductionObjective::getTargetQuantity)
                    .sum();
                
                double achievementRate = totalObjective > 0 
                    ? (totalProduced * 100.0 / totalObjective) 
                    : 0.0;
                
                // Stats par objectif
                List<ComparisonResponse.ObjectiveComparison> comparisons = lineObjectives.stream()
                    .map(obj -> {
                        ObjectiveStatsResponse stats = getObjectiveStats(obj.getId());
                        return new ComparisonResponse.ObjectiveComparison(
                            obj.getId(),
                            obj.getPeriod().name(),
                            obj.getTargetQuantity(),
                            stats.getAchieved(),
                            stats.getRate(),
                            stats.getStatus()
                        );
                    })
                    .collect(Collectors.toList());
                
                return new ComparisonResponse(
                    currentLine,
                    totalProduced,
                    totalObjective,
                    Math.round(achievementRate * 10.0) / 10.0,
                    0.0, // À calculer si nécessaire
                    comparisons
                );
            })
            .collect(Collectors.toList());
    }
}
```

## 6. Controller ProductionObjectiveController

```java
package com.autoreport.controller;

import com.autoreport.dto.*;
import com.autoreport.entity.*;
import com.autoreport.service.ProductionObjectiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/objectives")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class ProductionObjectiveController {
    
    @Autowired
    private ProductionObjectiveService objectiveService;
    
    /**
     * Lister les objectifs avec pagination et filtres
     */
    @GetMapping
    public ResponseEntity<PageResponse<ProductionObjective>> getObjectives(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String line,
        @RequestParam(required = false) ObjectivePeriod period,
        @RequestParam(required = false) Boolean active
    ) {
        Page<ProductionObjective> objectivePage = objectiveService.listObjectives(
            page, size, line, period, active
        );
        return ResponseEntity.ok(new PageResponse<>(objectivePage));
    }
    
    /**
     * Récupérer un objectif par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductionObjective> getObjective(@PathVariable Long id) {
        // Implementation avec findById
        return ResponseEntity.ok(/* ... */);
    }
    
    /**
     * Créer un objectif
     */
    @PostMapping
    public ResponseEntity<ProductionObjective> createObjective(
        @Valid @RequestBody ObjectiveRequest request,
        Authentication authentication
    ) {
        String userEmail = authentication.getName();
        ProductionObjective objective = objectiveService.createObjective(request, userEmail);
        return ResponseEntity.ok(objective);
    }
    
    /**
     * Mettre à jour un objectif
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductionObjective> updateObjective(
        @PathVariable Long id,
        @Valid @RequestBody ObjectiveRequest request
    ) {
        ProductionObjective objective = objectiveService.updateObjective(id, request);
        return ResponseEntity.ok(objective);
    }
    
    /**
     * Supprimer un objectif
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObjective(@PathVariable Long id) {
        objectiveService.deleteObjective(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Activer/désactiver un objectif
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductionObjective> toggleStatus(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> payload
    ) {
        Boolean active = payload.get("active");
        ProductionObjective objective = objectiveService.toggleStatus(id, active);
        return ResponseEntity.ok(objective);
    }
    
    /**
     * Récupérer les lignes de production disponibles
     */
    @GetMapping("/lines")
    public ResponseEntity<List<String>> getProductionLines() {
        List<String> lines = objectiveService.getProductionLines();
        return ResponseEntity.ok(lines);
    }
    
    /**
     * Récupérer les statistiques d'un objectif
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ObjectiveStatsResponse> getObjectiveStats(@PathVariable Long id) {
        ObjectiveStatsResponse stats = objectiveService.getObjectiveStats(id);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Comparer objectifs vs réalité
     */
    @GetMapping("/compare")
    public ResponseEntity<List<ComparisonResponse>> compareObjectivesVsReality(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        @RequestParam(required = false) String line
    ) {
        List<ComparisonResponse> comparison = objectiveService.compareObjectivesVsReality(
            startDate, endDate, line
        );
        return ResponseEntity.ok(comparison);
    }
}
```

## 7. Script SQL de Migration

```sql
-- Création de la table production_objectives
CREATE TABLE production_objectives (
    id BIGSERIAL PRIMARY KEY,
    line VARCHAR(100) NOT NULL,
    period VARCHAR(20) NOT NULL CHECK (period IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    target_quantity INTEGER NOT NULL CHECK (target_quantity > 0),
    target_quality_rate DECIMAL(5,2) NOT NULL CHECK (target_quality_rate BETWEEN 0 AND 100),
    max_defect_rate DECIMAL(5,2) NOT NULL CHECK (max_defect_rate BETWEEN 0 AND 100),
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT check_date_range CHECK (end_date IS NULL OR end_date >= start_date)
);

-- Index pour améliorer les performances
CREATE INDEX idx_objectives_line ON production_objectives(line);
CREATE INDEX idx_objectives_period ON production_objectives(period);
CREATE INDEX idx_objectives_active ON production_objectives(active);
CREATE INDEX idx_objectives_dates ON production_objectives(start_date, end_date);
CREATE INDEX idx_objectives_line_active ON production_objectives(line, active);
```

## Test avec cURL

```bash
# Créer un objectif
curl -X POST http://localhost:8080/api/objectives \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "line": "Line A",
    "period": "MONTHLY",
    "targetQuantity": 50000,
    "targetQualityRate": 95.0,
    "maxDefectRate": 5.0,
    "startDate": "2024-01-01",
    "active": true
  }'

# Lister les objectifs
curl http://localhost:8080/api/objectives?page=0&size=10 \
  -H "Authorization: Bearer TOKEN"

# Stats d'un objectif
curl http://localhost:8080/api/objectives/1/stats \
  -H "Authorization: Bearer TOKEN"

# Comparer objectifs vs réalité
curl "http://localhost:8080/api/objectives/compare?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer TOKEN"
```

## Points Clés pour la Soutenance

✅ **Planification stratégique** : Permet de définir des objectifs SMART
✅ **Suivi en temps réel** : Compare automatiquement réalisé vs objectif
✅ **Flexibilité** : Objectifs par ligne, par période, avec dates
✅ **Validation** : Empêche les chevauchements d'objectifs
✅ **Audit** : Traçabilité de qui a créé quel objectif
