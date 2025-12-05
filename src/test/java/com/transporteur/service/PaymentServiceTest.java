package com.transporteur.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

import com.transporteur.dto.PaymentRequest;
import com.transporteur.dto.PaymentResponse;
import com.transporteur.dto.PaymentStatusResponse;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.Payment;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.repository.PaymentRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private CompteRepository compteRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentService paymentService;

    private Compte compte;
    private Client client;
    private Transporteur transporteur;
    private Mission mission;
    private PaymentRequest paymentRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("client@test.com");

        // Setup compte
        compte = new Compte();
        compte.setId(1L);
        compte.setEmail("client@test.com");
        compte.setRole(Compte.Role.CLIENT);

        // Setup client
        client = new Client();
        client.setIdClient(1L);
        client.setCompte(compte);
        client.setNom("Doe");
        client.setPrenom("John");

        // Setup transporteur
        transporteur = new Transporteur();
        transporteur.setIdTransporteur(1L);
        transporteur.setNom("Smith");
        transporteur.setPrenom("Jane");

        // Setup mission
        mission = new Mission();
        mission.setIdMission(1L);
        mission.setClient(client);
        mission.setTransporteur(transporteur);
        mission.setStatut(Mission.StatutMission.PRIX_CONFIRME);
        mission.setProposedPrice(100.0);
        mission.setIsPaid(false);

        // Setup payment request
        paymentRequest = new PaymentRequest();
        paymentRequest.setMissionId(1L);
        paymentRequest.setAmount(100.0);
        paymentRequest.setCardNumber("1234567890123456");
        paymentRequest.setCardHolderName("John Doe");

        // Setup payment
        payment = new Payment();
        payment.setIdPayment(1L);
        payment.setMission(mission);
        payment.setClient(client);
        payment.setTransporteur(transporteur);
        payment.setAmount(100.0);
        payment.setTransactionId("TXN-12345678");
        payment.setPaymentStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setCardLastFour("3456");
    }

    @Test
    void testProcessPayment_Success() {
        // Given
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compte));
        when(clientRepository.findByCompte(compte)).thenReturn(Optional.of(client));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(paymentRepository.existsByMission(mission)).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);

        // When
        PaymentResponse response = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getPaymentId());
        assertEquals(1L, response.getMissionId());
        assertEquals(100.0, response.getAmount());
        assertEquals("COMPLETED", response.getPaymentStatus());
        assertNotNull(response.getTransactionId());
        verify(paymentRepository).save(any(Payment.class));
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void testProcessPayment_WrongClient_ThrowsException() {
        // Given
        Client otherClient = new Client();
        otherClient.setIdClient(2L);
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compte));
        when(clientRepository.findByCompte(compte)).thenReturn(Optional.of(otherClient));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.processPayment(paymentRequest));
        assertEquals("Vous n'êtes pas le client de cette mission", exception.getMessage());
    }

    @Test
    void testProcessPayment_PriceNotConfirmed_ThrowsException() {
        // Given
        mission.setStatut(Mission.StatutMission.EN_ATTENTE);
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compte));
        when(clientRepository.findByCompte(compte)).thenReturn(Optional.of(client));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.processPayment(paymentRequest));
        assertEquals("Le prix n'a pas été confirmé pour cette mission", exception.getMessage());
    }

    @Test
    void testProcessPayment_AlreadyPaid_ThrowsException() {
        // Given
        mission.setIsPaid(true);
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compte));
        when(clientRepository.findByCompte(compte)).thenReturn(Optional.of(client));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.processPayment(paymentRequest));
        assertEquals("Cette mission a déjà été payée", exception.getMessage());
    }

    @Test
    void testProcessPayment_AmountMismatch_ThrowsException() {
        // Given
        paymentRequest.setAmount(150.0);
        when(compteRepository.findByEmail("client@test.com")).thenReturn(Optional.of(compte));
        when(clientRepository.findByCompte(compte)).thenReturn(Optional.of(client));
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> paymentService.processPayment(paymentRequest));
        assertEquals("Le montant ne correspond pas au prix proposé", exception.getMessage());
    }

    @Test
    void testCheckPaymentStatus_Paid() {
        // Given
        mission.setIsPaid(true);
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));
        when(paymentRepository.findByMission(mission)).thenReturn(Optional.of(payment));

        // When
        PaymentStatusResponse response = paymentService.checkPaymentStatus(1L);

        // Then
        assertNotNull(response);
        assertTrue(response.getIsPaid());
        assertEquals(100.0, response.getAmount());
        assertEquals("COMPLETED", response.getPaymentStatus());
    }

    @Test
    void testCheckPaymentStatus_NotPaid() {
        // Given
        when(missionRepository.findById(1L)).thenReturn(Optional.of(mission));

        // When
        PaymentStatusResponse response = paymentService.checkPaymentStatus(1L);

        // Then
        assertNotNull(response);
        assertFalse(response.getIsPaid());
        assertEquals(100.0, response.getAmount());
        assertEquals("PENDING", response.getPaymentStatus());
    }
}

