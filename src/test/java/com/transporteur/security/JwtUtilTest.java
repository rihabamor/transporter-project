package com.transporteur.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "TestSecretKeyForJWTGeneration1234567890";
    private static final long EXPIRATION = 86400000L; // 24 heures

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", EXPIRATION);
    }

    @Test
    void testGenerateToken_WithUsername() {
        // Given
        String username = "test@example.com";

        // When
        String token = jwtUtil.generateToken(username);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testGenerateToken_WithUsernameAndRole() {
        // Given
        String username = "test@example.com";
        String role = "CLIENT";

        // When
        String token = jwtUtil.generateToken(username, role);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testExtractUsername_ValidToken() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        String extractedUsername = jwtUtil.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void testExtractRole_WithRole() {
        // Given
        String username = "test@example.com";
        String role = "TRANSPORTEUR";
        String token = jwtUtil.generateToken(username, role);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void testExtractRole_WithoutRole() {
        // Given
        String username = "test@example.com";
        String token = jwtUtil.generateToken(username);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertNull(extractedRole);
    }

    @Test
    void testExtractAllClaims() {
        // Given
        String username = "test@example.com";
        String role = "ADMIN";
        String token = jwtUtil.generateToken(username, role);

        // When
        Claims claims = jwtUtil.extractAllClaims(token);

        // Then
        assertNotNull(claims);
        assertEquals(username, claims.getSubject());
        assertEquals(role, claims.get("role"));
    }
}

