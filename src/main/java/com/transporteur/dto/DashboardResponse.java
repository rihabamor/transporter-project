package com.transporteur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class DashboardResponse {

    private Object profil;
    private Integer missionsEffectuees;
    private Integer missionsEnCours;
    private String messageBienvenue;

}
