package com.transporteur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransporteurAvailableResponse {

    private Long idTransporteur;
    private String nom;
    private String prenom;
    private String telephone;
    private String localisation;
    private Double noteMoyenne;
    private Boolean disponible;
}
