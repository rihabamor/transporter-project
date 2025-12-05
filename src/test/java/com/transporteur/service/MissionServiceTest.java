package com.transporteur.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.transporteur.dto.MissionRequest;
import com.transporteur.dto.MissionResponse;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.repository.PriceHistoryRepository;
import com.transporteur.repository.TransporteurRepository;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TransporteurRepository transporteurRepository;

    @Mock
    private CompteRepository compteRepository;

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MissionService missionService;

    private Compte compteClient;
    private Compte compteTransporteur;
    private Client client;
    private Transporteur transporteur;
    private Mission mission;
    private MissionRequest missionRequest;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("client@test.com");

        // Setup compte client
        compteClient = new Compte();
        compteClient.setId(1L);
        compteClient.setEmail("client@test.com");
        compteClient.setRole(Compte.Role.CLIENT);

        // Setup compte transporteur
        compteTransporteur = new Compte();
        compteTransporteur.setId(2L);
        compteTransporteur.setEmail("transporteur@test.com");
        compteTransporteur.setRole(Compte.Role.TRANSPORTEUR);

        // Setup client
        client = new Client();
        client.setIdClient(1L);
        client.setCompte(compteClient);
        client.setNom("Doe");
        client.setPrenom("John");

        // Setup transporteur
        transporteur = new Transporteur();
        transporteur.setIdTransporteur(1L);
        transporteur.setCompte(compteTransporteur);
        transporteur.setNom("Smith");
        transporteur.setPrenom("Jane");
        transporteur.setDisponible(true);

        // Setup mission
        mission = new Mission();
        mission.setIdMission(1L);
        mission.setClient(client);
        mission.setTransporteur(transporteur);
        mission.setDateMission(LocalDateTime.now().plusDays(1));
        mission.setLieuDepart("Tunis");
        mission.setLieuArrivee("Sfax");
        mission.setStatut(Mission.StatutMission.EN_ATTENTE);
        mission.setDateCreation(LocalDateTime.now());
        mission.setDescription("Test mission");

        // Setup mission request
        missionRequest = new MissionRequest();
        missionRequest.setTransporteurId(1L);
        missionRequest.setDateMission(LocalDateTime.now().plusDays(1));
        missionRequest.setLieuDepart("Tunis");
        missionRequest.setLieuArrivee("Sfax");
        missionRequest.setDescription("Test mission");
    }

    @Test
    void testGetAvailableTransporteurs_Success() {
        // Given
        Transporteur availableTransporteur = new Transporteur();
        availableTransporteur.setIdTransporteur(1L);
        availableTransporteur.setDisponible(true);
        availableTransporteur.setNom("Test");
        availableTransporteur.setPrenom("User");

        Transporteur unavailableTransporteur = new Transporteur();
        unavailableTransporteur.setIdTransporteur(2L);
        unavailableTransporteur.setDisponible(false);

        when(transporteurRepository.findAll()).thenReturn(
            Arrays.asList(availableTransporteur, unavailableTransporteur)
        );

        // When
        var result = missionService.getAvailableTransporteurs();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdTransporteur());
    }

    @Test
    void testCreateMission_Success() {
        // Given
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compteClient));
        when(clientRepository.findByCompte(compteClient)).thenReturn(Optional.of(client));
        when(transporteurRepository.findById(1L)).thenReturn(Optional.of(transporteur));
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        when(priceHistoryRepository.findByMissionOrderByChangeDateDesc(any(Mission.class)))
            .thenReturn(List.of());

        // When
        MissionResponse response = missionService.createMission(missionRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getIdMission());
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void testCreateMission_TransporteurNotAvailable_ThrowsException() {
        // Given
        transporteur.setDisponible(false);
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compteClient));
        when(clientRepository.findByCompte(compteClient)).thenReturn(Optional.of(client));
        when(transporteurRepository.findById(1L)).thenReturn(Optional.of(transporteur));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> missionService.createMission(missionRequest));
        assertEquals("Ce transporteur n'est pas disponible", exception.getMessage());
    }

    @Test
    void testGetClientMissions_Success() {
        // Given
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compteClient));
        when(clientRepository.findByCompte(compteClient)).thenReturn(Optional.of(client));
        when(missionRepository.findByClient(client)).thenReturn(Arrays.asList(mission));
        when(priceHistoryRepository.findByMissionOrderByChangeDateDesc(any(Mission.class)))
            .thenReturn(List.of());

        // When
        List<MissionResponse> result = missionService.getClientMissions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getIdMission());
    }

    @Test
    void testUpdateMissionStatus_Success() {
        // Given
        when(authentication.getName()).thenReturn("transporteur@test.com");
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.of(compteTransporteur));
        when(transporteurRepository.findByCompte(compteTransporteur)).thenReturn(Optional.of(transporteur));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        mission.setIsPaid(true);
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        when(priceHistoryRepository.findByMissionOrderByChangeDateDesc(any(Mission.class)))
            .thenReturn(List.of());

        // When
        MissionResponse response = missionService.updateMissionStatus(1L, "EN_COURS");

        // Then
        assertNotNull(response);
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void testUpdateMissionStatus_MissionNotPaid_ThrowsException() {
        // Given
        when(authentication.getName()).thenReturn("transporteur@test.com");
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.of(compteTransporteur));
        when(transporteurRepository.findByCompte(compteTransporteur)).thenReturn(Optional.of(transporteur));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        mission.setIsPaid(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> missionService.updateMissionStatus(1L, "EN_COURS"));
        assertEquals("La mission doit être payée avant de pouvoir commencer", exception.getMessage());
    }

    @Test
    void testCancelMission_Success() {
        // Given
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compteClient));
        when(clientRepository.findByCompte(compteClient)).thenReturn(Optional.of(client));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        when(priceHistoryRepository.findByMissionOrderByChangeDateDesc(any(Mission.class)))
            .thenReturn(List.of());

        // When
        MissionResponse response = missionService.cancelMission(1L);

        // Then
        assertNotNull(response);
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void testProposePrice_Success() {
        // Given
        when(authentication.getName()).thenReturn("transporteur@test.com");
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.of(compteTransporteur));
        when(transporteurRepository.findByCompte(compteTransporteur)).thenReturn(Optional.of(transporteur));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        when(priceHistoryRepository.findByMissionOrderByChangeDateDesc(any(Mission.class)))
            .thenReturn(List.of());

        // When
        MissionResponse response = missionService.proposePrice(1L, 100.0);

        // Then
        assertNotNull(response);
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void testConfirmPrice_Success() {
        // Given
        mission.setStatut(Mission.StatutMission.PRIX_PROPOSE);
        mission.setProposedPrice(100.0);
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compteClient));
        when(clientRepository.findByCompte(compteClient)).thenReturn(Optional.of(client));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        when(priceHistoryRepository.findByMissionOrderByChangeDateDesc(any(Mission.class)))
            .thenReturn(List.of());

        // When
        MissionResponse response = missionService.confirmPrice(1L);

        // Then
        assertNotNull(response);
        verify(missionRepository).save(any(Mission.class));
    }
}

