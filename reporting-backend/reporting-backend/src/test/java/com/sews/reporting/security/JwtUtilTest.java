package com.sews.reporting.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour JwtUtil
 * Couvre : génération token, extraction claims, validation, expiration
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "MyVerySecretKeyForTestingPurposesOnly1234567890";
    private static final long EXPIRATION_MS = 3600000; // 1 heure

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION_MS);
    }

    @Test
    void testGenerateToken_ShouldReturnValidToken() {
        // Arrange
        String email = "test@sews.com";
        String role = "ADMIN";

        // Act
        String token = jwtUtil.generateToken(email, role);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3, "JWT devrait avoir 3 parties séparées par des points");
    }

    @Test
    void testExtractEmail_ShouldReturnCorrectEmail() {
        // Arrange
        String email = "test@sews.com";
        String role = "MANAGER";
        String token = jwtUtil.generateToken(email, role);

        // Act
        String extractedEmail = jwtUtil.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    void testExtractRole_ShouldReturnCorrectRole() {
        // Arrange
        String email = "user@sews.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        // Act
        String extractedRole = jwtUtil.extractRole(token);

        // Assert
        assertEquals(role, extractedRole);
    }

    @Test
    void testIsTokenValid_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String email = "valid@sews.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(email, role);

        // Act
        boolean isValid = jwtUtil.isTokenValid(token, email);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_WithWrongEmail_ShouldReturnFalse() {
        // Arrange
        String email = "test@sews.com";
        String wrongEmail = "wrong@sews.com";
        String role = "USER";
        String token = jwtUtil.generateToken(email, role);

        // Act
        boolean isValid = jwtUtil.isTokenValid(token, wrongEmail);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_WithExpiredToken_ShouldReturnFalse() throws InterruptedException {
        // Arrange - créer un token avec une expiration très courte
        JwtUtil shortLivedJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(shortLivedJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(shortLivedJwtUtil, "expirationMs", 1L); // 1ms

        String email = "test@sews.com";
        String token = shortLivedJwtUtil.generateToken(email, "USER");

        // Act - attendre que le token expire
        Thread.sleep(10);
        boolean isValid = shortLivedJwtUtil.isTokenValid(token, email);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testGenerateToken_WithDifferentRoles_ShouldPreserveRole() {
        // Arrange & Act
        String adminToken = jwtUtil.generateToken("admin@sews.com", "ADMIN");
        String userToken = jwtUtil.generateToken("user@sews.com", "USER");
        String managerToken = jwtUtil.generateToken("manager@sews.com", "MANAGER");

        // Assert
        assertEquals("ADMIN", jwtUtil.extractRole(adminToken));
        assertEquals("USER", jwtUtil.extractRole(userToken));
        assertEquals("MANAGER", jwtUtil.extractRole(managerToken));
    }

    @Test
    void testExtractEmail_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtUtil.extractEmail(invalidToken));
    }

    @Test
    void testGenerateToken_ShouldProduceDifferentTokensForSameUser() {
        // Arrange
        String email = "test@sews.com";
        String role = "USER";

        // Act
        String token1 = jwtUtil.generateToken(email, role);
        try {
            Thread.sleep(10); // Assurer une timestamp différente
        } catch (InterruptedException e) {
            // Ignorer
        }
        String token2 = jwtUtil.generateToken(email, role);

        // Assert
        assertNotEquals(token1, token2, "Les tokens devraient être différents même pour le même utilisateur");
        
        // Mais extraire le même email et rôle
        assertEquals(jwtUtil.extractEmail(token1), jwtUtil.extractEmail(token2));
        assertEquals(jwtUtil.extractRole(token1), jwtUtil.extractRole(token2));
    }
}
