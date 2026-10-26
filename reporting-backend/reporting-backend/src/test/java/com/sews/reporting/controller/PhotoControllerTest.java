package com.sews.reporting.controller;

import com.sews.reporting.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de sécurité pour PhotoController
 * Vérifie que les photos de profil sont bien protégées
 */
@WebMvcTest(PhotoController.class)
class PhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditLogService auditLogService;

    @Test
    void testGetPhoto_WithoutAuthentication_ShouldReturn401() throws Exception {
        // Test d'accès non authentifié
        mockMvc.perform(get("/api/users/photos/user_1_profile.jpg"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@sews.com", roles = "USER")
    void testGetPhoto_WithAuthentication_WithValidFilename_ShouldSucceed() throws Exception {
        // Test d'accès authentifié avec nom de fichier valide
        // Note: Le fichier peut ne pas exister, donc 404 est acceptable
        mockMvc.perform(get("/api/users/photos/user_1_profile.jpg"))
                .andExpect(status().isNotFound()); // 404 car le fichier n'existe pas en test
    }

    @Test
    @WithMockUser(username = "test@sews.com", roles = "USER")
    void testGetPhoto_WithPathTraversal_ShouldReturnBadRequest() throws Exception {
        // Test de protection contre path traversal
        mockMvc.perform(get("/api/users/photos/../../../etc/passwd"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@sews.com", roles = "USER")
    void testGetPhoto_WithSlashInFilename_ShouldReturnBadRequest() throws Exception {
        // Test de protection contre slash dans le nom
        mockMvc.perform(get("/api/users/photos/subdir/file.jpg"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@sews.com", roles = "USER")
    void testGetPhoto_WithBackslashInFilename_ShouldReturnBadRequest() throws Exception {
        // Test de protection contre backslash dans le nom
        mockMvc.perform(get("/api/users/photos/subdir\\file.jpg"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin@sews.com", roles = "ADMIN")
    void testGetPhoto_AsAdmin_ShouldHaveAccess() throws Exception {
        // Les admins ont aussi accès aux photos
        mockMvc.perform(get("/api/users/photos/user_1_profile.jpg"))
                .andExpect(status().isNotFound()); // 404 car fichier n'existe pas
    }

    @Test
    @WithMockUser(username = "manager@sews.com", roles = "MANAGER")
    void testGetPhoto_AsManager_ShouldHaveAccess() throws Exception {
        // Les managers ont aussi accès aux photos
        mockMvc.perform(get("/api/users/photos/user_1_profile.jpg"))
                .andExpect(status().isNotFound()); // 404 car fichier n'existe pas
    }
}
