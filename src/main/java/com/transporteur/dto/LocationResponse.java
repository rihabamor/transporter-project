package com.transporteur.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les réponses de localisation en temps réel
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {
    private Long missionId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private Integer progressPercentage;  // 0-100
    private Double speed;  // km/h
    private String status;  // EN_COURS, TERMINEE, etc.
}
