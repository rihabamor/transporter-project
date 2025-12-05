package com.transporteur.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionRequest {

    @NotNull(message = "L'ID du transporteur est obligatoire")
    private Long transporteurId;

    @NotNull(message = "La date de la mission est obligatoire")
    private LocalDateTime dateMission;

    @NotBlank(message = "Le lieu de départ est obligatoire")
    private String lieuDepart;

    @NotBlank(message = "Le lieu d'arrivée est obligatoire")
    private String lieuArrivee;

    private String description;
}
