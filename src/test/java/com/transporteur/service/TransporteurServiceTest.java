package com.transporteur.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.transporteur.dto.ProfileRequest;
import com.transporteur.model.Compte;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.TransporteurRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransporteurServiceTest {

    @Mock
    private TransporteurRepository transporteurRepository;

    @Mock
    private CompteRepository compteRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransporteurService transporteurService;

    private Compte compte;
    private Transporteur transporteur;
    private ProfileRequest profileRequest;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("transporteur@test.com");

        // Setup compte
        compte = new Compte();
        compte.setId(1L);
        compte.setEmail("transporteur@test.com");
        compte.setRole(Compte.Role.TRANSPORTEUR);

        // Setup transporteur
        transporteur = new Transporteur();
        transporteur.setIdTransporteur(1L);
        transporteur.setCompte(compte);
        transporteur.setNom("Smith");
        transporteur.setPrenom("Jane");
        transporteur.setTelephone("123456789");
        transporteur.setLocalisation("Tunis");
        transporteur.setDisponible(true);

        // Setup profile request
        profileRequest = new ProfileRequest();
        profileRequest.setNom("Updated");
        profileRequest.setPrenom("Name");
        profileRequest.setTelephone("987654321");
        profileRequest.setLocalisation("Sfax");
        profileRequest.setEmail("newemail@test.com");
    }

    @Test
    void testAddTransporteur_Success() {
        // Given
        when(transporteurRepository.save(transporteur)).thenReturn(transporteur);

        // When
        Transporteur result = transporteurService.addTransporteur(transporteur);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIdTransporteur());
        verify(transporteurRepository).save(transporteur);
    }

    @Test
    void testGetAllTransporteurs_Success() {
        // Given
        Transporteur transporteur2 = new Transporteur();
        transporteur2.setIdTransporteur(2L);
        when(transporteurRepository.findAll()).thenReturn(Arrays.asList(transporteur, transporteur2));

        // When
        List<Transporteur> result = transporteurService.getAllTransporteurs();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(transporteurRepository).findAll();
    }

    @Test
    void testGetProfilTransporteur_Success() {
        // Given
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.of(compte));
        when(transporteurRepository.findByCompte(compte)).thenReturn(Optional.of(transporteur));

        // When
        Transporteur result = transporteurService.getProfilTransporteur();

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getIdTransporteur());
        verify(compteRepository).findByEmail("transporteur@test.com");
        verify(transporteurRepository).findByCompte(compte);
    }

    @Test
    void testGetProfilTransporteur_CompteNotFound_ThrowsException() {
        // Given
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> transporteurService.getProfilTransporteur());
        assertEquals("Compte non trouvé", exception.getMessage());
    }

    @Test
    void testUpdateProfilTransporteur_Success() {
        // Given
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.of(compte));
        when(transporteurRepository.findByCompte(compte)).thenReturn(Optional.of(transporteur));
        when(transporteurRepository.save(any(Transporteur.class))).thenReturn(transporteur);
        when(compteRepository.save(any(Compte.class))).thenReturn(compte);

        // When
        Transporteur result = transporteurService.updateProfilTransporteur(profileRequest);

        // Then
        assertNotNull(result);
        verify(transporteurRepository).save(any(Transporteur.class));
        verify(compteRepository).save(any(Compte.class));
    }

    @Test
    void testUpdateDisponibilite_Success() {
        // Given
        when(compteRepository.findByEmail("transporteur@test.com")).thenReturn(Optional.of(compte));
        when(transporteurRepository.findByCompte(compte)).thenReturn(Optional.of(transporteur));
        when(transporteurRepository.save(any(Transporteur.class))).thenReturn(transporteur);

        // When
        Transporteur result = transporteurService.updateDisponibilite(false);

        // Then
        assertNotNull(result);
        verify(transporteurRepository).save(any(Transporteur.class));
        verify(transporteurRepository).flush();
    }
}

