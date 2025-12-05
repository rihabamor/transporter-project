package com.transporteur.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporteur.dto.LocationResponse;
import com.transporteur.service.TrackingService;

/**
 * Contrôleur pour le tracking GPS en temps réel
 */
@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4201"})
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Obtenir la position GPS actuelle d'une mission
     * GET /api/tracking/missions/{missionId}/location
     */
    @GetMapping("/missions/{missionId}/location")
    public ResponseEntity<LocationResponse> getCurrentLocation(@PathVariable Long missionId) {
        try {
            LocationResponse location = trackingService.getCurrentLocation(missionId);
            return ResponseEntity.ok(location);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
