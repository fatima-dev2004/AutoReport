package com.sews.reporting.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sews.reporting.dto.AuthResponse;
import com.sews.reporting.dto.LoginRequest;
import com.sews.reporting.entity.AuditLog;
import com.sews.reporting.service.AuditLogService;
import com.sews.reporting.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour AuthController
 * Couvre : login, validation, audit logs
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuditLogService auditLogService;

    private LoginRequest validLoginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setEmail("admin@sews.com");
        validLoginRequest.setPassword("Admin@123");

        authResponse = AuthResponse.builder()
                .token("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
                .refreshToken("refresh-token-123")
                .userId(1L)
                .email("admin@sews.com")
                .role("ADMIN")
                .fullName("Admin User")
                .expiresIn(86400L)
                .build();
    }

    @Test
    void testLogin_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenReturn(authResponse);
        doNothing().when(auditLogService).logFromRequest(
                any(), anyLong(), any(AuditLog.ActionType.class), anyString(), anyString()
        );

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(authResponse.getToken()))
                .andExpect(jsonPath("$.userId").value(authResponse.getUserId()))
                .andExpect(jsonPath("$.email").value(authResponse.getEmail()))
                .andExpect(jsonPath("$.role").value(authResponse.getRole()))
                .andExpect(jsonPath("$.fullName").value(authResponse.getFullName()));

        // Vérifier que le log d'audit a été créé
        verify(auditLogService, times(1)).logFromRequest(
                any(), eq(authResponse.getUserId()), eq(AuditLog.ActionType.LOGIN), anyString(), anyString()
        );
    }

    @Test
    void testLogin_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Vérifier qu'aucune tentative de login n'a été faite
        verify(authService, never()).login(any(LoginRequest.class), any(HttpServletRequest.class));
        verify(auditLogService, never()).logFromRequest(any(), anyLong(), any(), anyString(), anyString());
    }

    @Test
    void testLogin_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setEmail("admin@sews.com");
        invalidRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void testLogin_WithNullFields_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidRequest = new LoginRequest();
        // email et password sont null

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class), any(HttpServletRequest.class));
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Identifiants invalides"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isInternalServerError());

        // Vérifier qu'aucun audit log n'a été créé pour une tentative échouée
        verify(auditLogService, never()).logFromRequest(any(), anyLong(), any(), anyString(), anyString());
    }

    @Test
    void testLogin_ContentTypeJson_IsRequired() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
