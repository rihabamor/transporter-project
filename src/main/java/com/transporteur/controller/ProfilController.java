package com.transporteur.controller;

import java.util.Arrays;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporteur.dto.AdminProfileResponse;
import com.transporteur.dto.DashboardResponse;
import com.transporteur.dto.DisponibiliteRequest;
import com.transporteur.dto.ProfileRequest;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.service.ClientService;
import com.transporteur.service.TransporteurService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profil")
@CrossOrigin(origins = "http://localhost:4200")

public class ProfilController {

    private final ClientService clientService;
    private final TransporteurService transporteurService;
    private final MissionRepository missionRepository;
    private final CompteRepository compteRepository;

    public ProfilController(ClientService clientService, TransporteurService transporteurService, 
                           MissionRepository missionRepository, CompteRepository compteRepository) {
        this.clientService = clientService;
        this.transporteurService = transporteurService;
        this.missionRepository = missionRepository;
        this.compteRepository = compteRepository;
    }

    // Endpoint pour l'admin
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getProfilAdmin() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
            
            if (compte.getRole() != Compte.Role.ADMIN) {
                throw new RuntimeException("Accès refusé");
            }
            
            AdminProfileResponse response = new AdminProfileResponse();
            response.setAccountId(compte.getId());
            response.setEmail(compte.getEmail());
            response.setRole(compte.getRole().name());
            response.setDateCreation(compte.getDateCreation());
            response.setPermissions(Arrays.asList("VIEW_ACCOUNTS", "VIEW_TRANSACTIONS", "VIEW_STATISTICS"));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoints pour le client
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> getProfilClient() {
        try {
            Client profil = clientService.getProfilClient();
            
            // Récupérer les vraies statistiques de missions
            long missionsTerminees = missionRepository.countByClientAndStatut(profil, Mission.StatutMission.TERMINEE);
            long missionsEnCours = missionRepository.countActiveMissionsByClient(profil);
            
            DashboardResponse dashboard = new DashboardResponse(
                profil,
                (int) missionsTerminees,
                (int) missionsEnCours,
                "Bienvenue sur votre tableau de bord client"
            );
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> updateProfilClient(@Valid @RequestBody ProfileRequest request) {
        try {
            Object profil = clientService.updateProfilClient(request);
            return ResponseEntity.ok(profil);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoints pour le transporteur
    @GetMapping("/transporteur")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<?> getProfilTransporteur() {
        try {
            Transporteur profil = transporteurService.getProfilTransporteur();
            
            // Récupérer les vraies statistiques de missions
            long missionsTerminees = missionRepository.countByTransporteurAndStatut(profil, Mission.StatutMission.TERMINEE);
            long missionsEnCours = missionRepository.countActiveMissionsByTransporteur(profil);
            
            DashboardResponse dashboard = new DashboardResponse(
                profil,
                (int) missionsTerminees,
                (int) missionsEnCours,
                "Bienvenue sur votre tableau de bord transporteur"
            );
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/transporteur")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<?> updateProfilTransporteur(@Valid @RequestBody ProfileRequest request) {
        try {
            Object profil = transporteurService.updateProfilTransporteur(request);
            return ResponseEntity.ok(profil);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Gestion de la disponibilité du transporteur
    @PutMapping("/transporteur/disponibilite")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<?> updateDisponibilite(@RequestBody DisponibiliteRequest request) {
        try {
            Object transporteur = transporteurService.updateDisponibilite(request.getDisponible());
            return ResponseEntity.ok(transporteur);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
