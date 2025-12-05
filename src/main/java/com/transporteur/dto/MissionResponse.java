package com.transporteur.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionResponse {

    private Long idMission;
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private Long transporteurId;
    private String transporteurNom;
    private String transporteurPrenom;
    private LocalDateTime dateMission;
    private String lieuDepart;
    private String lieuArrivee;
    private String statut;
    private LocalDateTime dateCreation;
    private String description;
    
    // Champs de paiement
    private Double proposedPrice;
    private Boolean priceConfirmed;
    private Boolean isPaid;
    
    // Historique des changements de prix
    private List<PriceHistoryResponse> priceHistory;
}
