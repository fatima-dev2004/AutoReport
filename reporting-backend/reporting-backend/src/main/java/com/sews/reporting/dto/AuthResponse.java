package com.sews.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private String role;
    private String fullName;
    private Long expiresIn; // Durée de validité du token en secondes
}
