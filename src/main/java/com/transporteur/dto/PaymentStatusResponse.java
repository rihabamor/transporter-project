package com.transporteur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour vérifier le statut de paiement d'une mission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    
    private Long missionId;
    private Boolean isPaid;
    private Double amount;
    private String paymentStatus;
    private String message;
}
