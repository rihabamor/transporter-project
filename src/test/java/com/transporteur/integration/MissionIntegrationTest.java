package com.transporteur.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

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
import com.transporteur.dto.MissionRequest;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.repository.TransporteurRepository;
import com.transporteur.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
class MissionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TransporteurRepository transporteurRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String clientToken;
    private String transporteurToken;
    private Client client;
    private Transporteur transporteur;

    @BeforeEach
    void setUp() {
        compteRepository.deleteAll();
        clientRepository.deleteAll();
        transporteurRepository.deleteAll();
        missionRepository.deleteAll();

        // Create client
        Compte compteClient = new Compte();
        compteClient.setEmail("client@test.com");
        compteClient.setPassword(passwordEncoder.encode("password123"));
        compteClient.setRole(Compte.Role.CLIENT);
        compteClient = compteRepository.save(compteClient);

        client = new Client();
        client.setCompte(compteClient);
        client.setNom("Client");
        client.setPrenom("Test");
        client = clientRepository.save(client);

        clientToken = jwtUtil.generateToken(compteClient.getEmail(), compteClient.getRole().name());

        // Create transporteur
        Compte compteTransporteur = new Compte();
        compteTransporteur.setEmail("transporteur@test.com");
        compteTransporteur.setPassword(passwordEncoder.encode("password123"));
        compteTransporteur.setRole(Compte.Role.TRANSPORTEUR);
        compteTransporteur = compteRepository.save(compteTransporteur);

        transporteur = new Transporteur();
        transporteur.setCompte(compteTransporteur);
        transporteur.setNom("Transporteur");
        transporteur.setPrenom("Test");
        transporteur.setDisponible(true);
        transporteur = transporteurRepository.save(transporteur);

        transporteurToken = jwtUtil.generateToken(compteTransporteur.getEmail(), compteTransporteur.getRole().name());
    }

    @Test
    void testGetAvailableTransporteurs_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/missions/transporteurs/disponibles")
                .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateMission_Success() throws Exception {
        // Given
        MissionRequest request = new MissionRequest();
        request.setTransporteurId(transporteur.getIdTransporteur());
        request.setDateMission(LocalDateTime.now().plusDays(1));
        request.setLieuDepart("Tunis");
        request.setLieuArrivee("Sfax");
        request.setDescription("Test mission");

        // When & Then
        mockMvc.perform(post("/api/missions")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMission").exists());
    }

    @Test
    void testGetClientMissions_Success() throws Exception {
        // Given - Create a mission
        Mission mission = new Mission();
        mission.setClient(client);
        mission.setTransporteur(transporteur);
        mission.setDateMission(LocalDateTime.now().plusDays(1));
        mission.setLieuDepart("Tunis");
        mission.setLieuArrivee("Sfax");
        mission.setStatut(Mission.StatutMission.EN_ATTENTE);
        missionRepository.save(mission);

        // When & Then
        mockMvc.perform(get("/api/missions/client")
                .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransporteurMissions_Success() throws Exception {
        // Given - Create a mission
        Mission mission = new Mission();
        mission.setClient(client);
        mission.setTransporteur(transporteur);
        mission.setDateMission(LocalDateTime.now().plusDays(1));
        mission.setLieuDepart("Tunis");
        mission.setLieuArrivee("Sfax");
        mission.setStatut(Mission.StatutMission.EN_ATTENTE);
        missionRepository.save(mission);

        // When & Then
        mockMvc.perform(get("/api/missions/transporteur")
                .header("Authorization", "Bearer " + transporteurToken))
                .andExpect(status().isOk());
    }
}

