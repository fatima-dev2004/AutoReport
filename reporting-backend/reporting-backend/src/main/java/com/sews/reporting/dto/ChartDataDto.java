package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    private String lineName;    // Nom de la ligne (A, B, C, D)
    private Double quantity;    // Quantité produite
    private Integer defects;    // Nombre de défauts
}
