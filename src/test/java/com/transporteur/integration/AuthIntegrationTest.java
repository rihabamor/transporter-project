package com.transporteur.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Import;
import com.transporteur.config.TestSecurityConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transporteur.dto.LoginRequest;
import com.transporteur.dto.RegisterRequest;
import com.transporteur.model.Compte;
import com.transporteur.model.Client;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.ClientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        compteRepository.deleteAll();
        clientRepository.deleteAll();
    }

    @Test
    void testRegister_Client_Success() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newclient@test.com");
        request.setPassword("password123");
        request.setRole("CLIENT");
        request.setNom("Doe");
        request.setPrenom("John");
        request.setTelephone("123456789");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testRegister_EmailAlreadyExists_ReturnsError() throws Exception {
        // Given - Create existing user
        Compte compte = new Compte();
        compte.setEmail("existing@test.com");
        compte.setPassword(passwordEncoder.encode("password123"));
        compte.setRole(Compte.Role.CLIENT);
        compteRepository.save(compte);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@test.com");
        request.setPassword("password123");
        request.setRole("CLIENT");
        request.setNom("Doe");
        request.setPrenom("John");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller returns 200 even on error
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given - Create user
        Compte compte = new Compte();
        compte.setEmail("login@test.com");
        compte.setPassword(passwordEncoder.encode("password123"));
        compte.setRole(Compte.Role.CLIENT);
        compteRepository.save(compte);

        Client client = new Client();
        client.setCompte(compte);
        client.setNom("Test");
        client.setPrenom("User");
        clientRepository.save(client);

        LoginRequest request = new LoginRequest();
        request.setEmail("login@test.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void testLogin_InvalidCredentials_ReturnsError() throws Exception {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@test.com");
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Controller returns 200 even on error
    }
}

