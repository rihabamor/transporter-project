package com.transporteur.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporteur.dto.MissionRequest;
import com.transporteur.dto.MissionResponse;
import com.transporteur.dto.PriceProposalRequest;
import com.transporteur.dto.TransporteurAvailableResponse;
import com.transporteur.dto.UpdatePriceRequest;
import com.transporteur.service.MissionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/missions")
@CrossOrigin(origins = "http://localhost:4200")
public class MissionController {

    private final MissionService missionService;

    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    /**
     * Obtenir la liste des transporteurs disponibles
     * Accessible par les CLIENTS pour choisir un transporteur
     */
    @GetMapping("/transporteurs/disponibles")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<TransporteurAvailableResponse>> getAvailableTransporteurs() {
        try {
            List<TransporteurAvailableResponse> transporteurs = missionService.getAvailableTransporteurs();
            return ResponseEntity.ok(transporteurs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Créer une nouvelle mission
     * Accessible uniquement par les CLIENTS
     */
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> createMission(@Valid @RequestBody MissionRequest request) {
        try {
            MissionResponse mission = missionService.createMission(request);
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtenir toutes les missions du client connecté
     */
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<MissionResponse>> getClientMissions() {
        try {
            List<MissionResponse> missions = missionService.getClientMissions();
            return ResponseEntity.ok(missions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtenir toutes les missions du transporteur connecté
     */
    @GetMapping("/transporteur")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<List<MissionResponse>> getTransporteurMissions() {
        try {
            List<MissionResponse> missions = missionService.getTransporteurMissions();
            return ResponseEntity.ok(missions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtenir une mission par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENT', 'TRANSPORTEUR')")
    public ResponseEntity<?> getMissionById(@PathVariable Long id) {
        try {
            MissionResponse mission = missionService.getMissionById(id);
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtenir le numéro de téléphone du transporteur d'une mission
     * Accessible uniquement par le CLIENT qui a créé la mission
     */
    @GetMapping("/{id}/transporteur/contact")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> getTransporteurContact(@PathVariable Long id) {
        try {
            Map<String, String> contact = missionService.getTransporteurContact(id);
            return ResponseEntity.ok(contact);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Mettre à jour le statut d'une mission
     * Accessible uniquement par le TRANSPORTEUR assigné
     */
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<?> updateMissionStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        try {
            MissionResponse mission = missionService.updateMissionStatus(id, request.getStatut());
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Annuler une mission
     * Accessible uniquement par le CLIENT qui a créé la mission
     */
    @PutMapping("/{id}/annuler")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> cancelMission(@PathVariable Long id) {
        try {
            MissionResponse mission = missionService.cancelMission(id);
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Proposer un prix pour une mission
     * Accessible uniquement par le TRANSPORTEUR assigné
     */
    @PostMapping("/{id}/propose-price")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<?> proposePrice(
            @PathVariable Long id,
            @Valid @RequestBody PriceProposalRequest request) {
        try {
            MissionResponse mission = missionService.proposePrice(id, request.getProposedPrice());
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Confirmer le prix d'une mission
     * Accessible uniquement par le CLIENT qui a créé la mission
     */
    @PostMapping("/{id}/confirm-price")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> confirmPrice(@PathVariable Long id) {
        try {
            MissionResponse mission = missionService.confirmPrice(id);
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Modifier le prix proposé (avant confirmation du client)
     * Accessible uniquement par le TRANSPORTEUR assigné
     */
    @PutMapping("/{id}/update-price")
    @PreAuthorize("hasRole('TRANSPORTEUR')")
    public ResponseEntity<?> updateProposedPrice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePriceRequest request) {
        try {
            MissionResponse mission = missionService.updateProposedPrice(
                id, 
                request.getNewPrice(), 
                request.getReason()
            );
            return ResponseEntity.ok(mission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Classe interne pour la mise à jour du statut
    public static class StatusUpdateRequest {
        private String statut;

        public String getStatut() {
            return statut;
        }

        public void setStatut(String statut) {
            this.statut = statut;
        }
    }
}
