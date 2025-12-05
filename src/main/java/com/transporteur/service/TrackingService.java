package com.transporteur.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.transporteur.dto.LocationResponse;
import com.transporteur.model.Mission;
import com.transporteur.repository.MissionRepository;

/**
 * Service simplifié pour la simulation de tracking GPS
 * Utilise un polling HTTP simple (pas de WebSocket)
 */
@Service
public class TrackingService {

    private final MissionRepository missionRepository;
    
    // Stocker les données de simulation pour chaque mission
    private final Map<Long, TrackingData> trackingCache = new ConcurrentHashMap<>();
    
    // Coordonnées de départ (Tunisia - Point 1)
    private static final double START_LAT = 35.669948;
    private static final double START_LON = 10.591675;
    
    // Coordonnées d'arrivée (Tunisia - Point 2)
    private static final double END_LAT = 35.522941;
    private static final double END_LON = 11.031608;
    
    // Durée totale du trajet (en minutes) - Simulation accélérée pour demo
    private static final long TRIP_DURATION_MINUTES = 5;  // 5 minutes au lieu de 60

    public TrackingService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    /**
     * Obtenir la position actuelle d'une mission
     */
    public LocationResponse getCurrentLocation(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Si la mission n'est pas EN_COURS, retourner null
        if (mission.getStatut() != Mission.StatutMission.EN_COURS) {
            return buildLocationResponse(mission, null, null, 0, 0.0);
        }

        // Obtenir ou créer les données de tracking
        TrackingData trackingData = trackingCache.computeIfAbsent(missionId, 
            id -> new TrackingData(LocalDateTime.now()));

        // Calculer la position actuelle basée sur le temps écoulé
        LocationPoint currentPosition = calculateCurrentPosition(trackingData);

        // Calculer le pourcentage de progression
        int progress = calculateProgress(trackingData);

        return buildLocationResponse(mission, currentPosition.lat, currentPosition.lon, progress, currentPosition.speed);
    }

    /**
     * Nettoyer le cache quand une mission se termine
     */
    public void clearTracking(Long missionId) {
        trackingCache.remove(missionId);
    }

    /**
     * Calculer la position actuelle basée sur le temps écoulé
     */
    private LocationPoint calculateCurrentPosition(TrackingData trackingData) {
        long elapsedSeconds = ChronoUnit.SECONDS.between(trackingData.startTime, LocalDateTime.now());
        long totalSeconds = TRIP_DURATION_MINUTES * 60;
        
        // Si le trajet est terminé, retourner la position finale
        if (elapsedSeconds >= totalSeconds) {
            return new LocationPoint(END_LAT, END_LON, 0.0);
        }

        // Calculer le ratio de progression (0.0 à 1.0)
        double ratio = (double) elapsedSeconds / totalSeconds;

        // Interpolation linéaire entre le début et la fin
        double lat = START_LAT + (END_LAT - START_LAT) * ratio;
        double lon = START_LON + (END_LON - START_LON) * ratio;
        
        // Vitesse simulée aléatoire entre 50 et 80 km/h
        double speed = 50 + (Math.random() * 30);

        return new LocationPoint(lat, lon, speed);
    }

    /**
     * Calculer le pourcentage de progression
     */
    private int calculateProgress(TrackingData trackingData) {
        long elapsedSeconds = ChronoUnit.SECONDS.between(trackingData.startTime, LocalDateTime.now());
        long totalSeconds = TRIP_DURATION_MINUTES * 60;
        
        if (elapsedSeconds >= totalSeconds) {
            return 100;
        }

        return (int) ((double) elapsedSeconds / totalSeconds * 100);
    }

    /**
     * Construire la réponse LocationResponse
     */
    private LocationResponse buildLocationResponse(Mission mission, Double lat, Double lon, int progress, double speed) {
        LocationResponse response = new LocationResponse();
        response.setMissionId(mission.getIdMission());
        response.setLatitude(lat);
        response.setLongitude(lon);
        response.setTimestamp(LocalDateTime.now());
        response.setProgressPercentage(progress);
        response.setSpeed(speed);
        response.setStatus(mission.getStatut().name());
        return response;
    }

    /**
     * Classe interne pour stocker les données de tracking
     */
    private static class TrackingData {
        LocalDateTime startTime;

        TrackingData(LocalDateTime startTime) {
            this.startTime = startTime;
        }
    }

    /**
     * Classe interne pour représenter un point GPS
     */
    private static class LocationPoint {
        double lat;
        double lon;
        double speed;

        LocationPoint(double lat, double lon, double speed) {
            this.lat = lat;
            this.lon = lon;
            this.speed = speed;
        }
    }
}
