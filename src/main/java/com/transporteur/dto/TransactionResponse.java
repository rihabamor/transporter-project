package com.transporteur.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour afficher les transactions (Admin uniquement)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    
    private Long paymentId;
    private Long missionId;
    private String missionLieuDepart;
    private String missionLieuArrivee;
    private LocalDateTime missionDate;
    
    private Long clientId;
    private String clientNom;
    private String clientPrenom;
    private String clientEmail;
    
    private Long transporteurId;
    private String transporteurNom;
    private String transporteurPrenom;
    private String transporteurEmail;
    
    private Double amount;
    private String cardLastFour;
    private String transactionId;
    private String paymentStatus;
    private LocalDateTime paymentDate;
}
