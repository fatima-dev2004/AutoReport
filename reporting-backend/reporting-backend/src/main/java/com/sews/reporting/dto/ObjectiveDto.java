package com.sews.reporting.dto;

import com.sews.reporting.entity.ProductionObjective;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObjectiveDto {
    private Long id;
    private String line;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer targetQuantity;
    private Double targetQualityRate;
    private Integer maxDefects;
    private Boolean active;
    private String notes;
    
    // Constructeur depuis entité
    public ObjectiveDto(ProductionObjective objective) {
        this.id = objective.getId();
        this.line = objective.getLine();
        this.periodType = objective.getPeriodType().name();
        this.startDate = objective.getStartDate();
        this.endDate = objective.getEndDate();
        this.targetQuantity = objective.getTargetQuantity();
        this.targetQualityRate = objective.getTargetQualityRate();
        this.maxDefects = objective.getMaxDefects();
        this.active = objective.getActive();
        this.notes = objective.getNotes();
    }
}
