package com.transporteur.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporteur.dto.AuthResponse;
import com.transporteur.dto.RegisterRequest;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.TransporteurRepository;
import com.transporteur.security.JwtUtil;

@Service
public class AuthService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final TransporteurRepository transporteurRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(CompteRepository compteRepository, 
                      ClientRepository clientRepository,
                      TransporteurRepository transporteurRepository,
                      PasswordEncoder passwordEncoder, 
                      AuthenticationManager authenticationManager, 
                      JwtUtil jwtUtil) {
        this.compteRepository = compteRepository;
        this.clientRepository = clientRepository;
        this.transporteurRepository = transporteurRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validation supplémentaire
        if (compteRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé !");
        }

        // Création du compte
        Compte compte = new Compte();
        compte.setEmail(request.getEmail());
        compte.setPassword(passwordEncoder.encode(request.getPassword()));
        compte.setRole(Compte.Role.valueOf(request.getRole().toUpperCase()));
        
        Compte savedCompte = compteRepository.save(compte);

        // Création du profil spécifique selon le rôle
        if (request.getRole().equalsIgnoreCase("CLIENT")) {
            createClientProfile(savedCompte, request);
        } else if (request.getRole().equalsIgnoreCase("TRANSPORTEUR")) {
            createTransporteurProfile(savedCompte, request);
        }

        String token = jwtUtil.generateToken(savedCompte.getEmail());
        return new AuthResponse(token);
    }

    private void createClientProfile(Compte compte, RegisterRequest request) {
        if (request.getNom() == null || request.getPrenom() == null) {
            throw new RuntimeException("Le nom et prénom sont obligatoires pour un client");
        }

        Client client = new Client();
        client.setCompte(compte);
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setTelephone(request.getTelephone());
        client.setAdresse(request.getAdresse());
        client.setVille(request.getVille());
        
        clientRepository.save(client);
    }

    private void createTransporteurProfile(Compte compte, RegisterRequest request) {
        if (request.getNom() == null || request.getPrenom() == null) {
            throw new RuntimeException("Le nom et prénom sont obligatoires pour un transporteur");
        }

        Transporteur transporteur = new Transporteur();
        transporteur.setCompte(compte);
        transporteur.setNom(request.getNom());
        transporteur.setPrenom(request.getPrenom());
        transporteur.setTelephone(request.getTelephone());
        transporteur.setLocalisation(request.getLocalisation());
        
        transporteurRepository.save(transporteur);
    }

    public AuthResponse login(String email, String password) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
            );
            String token = jwtUtil.generateToken(email);
            return new AuthResponse(token);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
    }
}