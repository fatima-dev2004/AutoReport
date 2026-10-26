package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemSettingsDto {
    private Map<String, String> smtp;
    private Map<String, String> company;
    private Map<String, String> scheduling;
    private Map<String, String> general;
}
