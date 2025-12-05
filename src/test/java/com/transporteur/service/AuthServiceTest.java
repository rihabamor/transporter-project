package com.transporteur.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.transporteur.dto.AuthResponse;
import com.transporteur.dto.RegisterRequest;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.TransporteurRepository;
import com.transporteur.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CompteRepository compteRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private TransporteurRepository transporteurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest clientRequest;
    private RegisterRequest transporteurRequest;
    private Compte compte;
    private Client client;
    private Transporteur transporteur;

    @BeforeEach
    void setUp() {
        // Setup client request
        clientRequest = new RegisterRequest();
        clientRequest.setEmail("client@test.com");
        clientRequest.setPassword("password123");
        clientRequest.setRole("CLIENT");
        clientRequest.setNom("Doe");
        clientRequest.setPrenom("John");
        clientRequest.setTelephone("123456789");
        clientRequest.setAdresse("123 Test St");
        clientRequest.setVille("Tunis");

        // Setup transporteur request
        transporteurRequest = new RegisterRequest();
        transporteurRequest.setEmail("transporteur@test.com");
        transporteurRequest.setPassword("password123");
        transporteurRequest.setRole("TRANSPORTEUR");
        transporteurRequest.setNom("Smith");
        transporteurRequest.setPrenom("Jane");
        transporteurRequest.setTelephone("987654321");
        transporteurRequest.setLocalisation("Tunis");

        // Setup compte
        compte = new Compte();
        compte.setId(1L);
        compte.setEmail("client@test.com");
        compte.setPassword("encodedPassword");
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
        transporteur.setCompte(compte);
        transporteur.setNom("Smith");
        transporteur.setPrenom("Jane");
    }

    @Test
    void testRegister_Client_Success() {
        // Given
        when(compteRepository.existsByEmail(clientRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(clientRequest.getPassword())).thenReturn("encodedPassword");
        when(compteRepository.save(any(Compte.class))).thenReturn(compte);
        when(clientRepository.save(any(Client.class))).thenReturn(client);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-token");

        // When
        AuthResponse response = authService.register(clientRequest);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        verify(compteRepository).save(any(Compte.class));
        verify(clientRepository).save(any(Client.class));
        verify(transporteurRepository, never()).save(any(Transporteur.class));
    }

    @Test
    void testRegister_Transporteur_Success() {
        // Given
        compte.setRole(Compte.Role.TRANSPORTEUR);
        when(compteRepository.existsByEmail(transporteurRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(transporteurRequest.getPassword())).thenReturn("encodedPassword");
        when(compteRepository.save(any(Compte.class))).thenReturn(compte);
        when(transporteurRepository.save(any(Transporteur.class))).thenReturn(transporteur);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("test-token");

        // When
        AuthResponse response = authService.register(transporteurRequest);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        verify(compteRepository).save(any(Compte.class));
        verify(transporteurRepository).save(any(Transporteur.class));
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testRegister_EmailAlreadyExists_ThrowsException() {
        // Given
        when(compteRepository.existsByEmail(clientRequest.getEmail())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(clientRequest));
        assertEquals("Email déjà utilisé !", exception.getMessage());
        verify(compteRepository, never()).save(any(Compte.class));
    }

    @Test
    void testRegister_Client_MissingName_ThrowsException() {
        // Given
        clientRequest.setNom(null);
        when(compteRepository.existsByEmail(clientRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(clientRequest.getPassword())).thenReturn("encodedPassword");
        when(compteRepository.save(any(Compte.class))).thenReturn(compte);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.register(clientRequest));
        assertEquals("Le nom et prénom sont obligatoires pour un client", exception.getMessage());
    }

    @Test
    void testLogin_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(compteRepository.findByEmail(email)).thenReturn(Optional.of(compte));
        when(jwtUtil.generateToken(email, compte.getRole().name())).thenReturn("test-token");

        // When
        AuthResponse response = authService.login(email, password);

        // Then
        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(compteRepository).findByEmail(email);
    }

    @Test
    void testLogin_BadCredentials_ThrowsException() {
        // Given
        String email = "test@example.com";
        String password = "wrongPassword";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(email, password));
        assertEquals("Email ou mot de passe incorrect", exception.getMessage());
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        // Given
        String email = "notfound@example.com";
        String password = "password123";
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(compteRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> authService.login(email, password));
        assertEquals("Utilisateur non trouvé", exception.getMessage());
    }
}

