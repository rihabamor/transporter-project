package com.transporteur.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse après un paiement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private Long paymentId;
    private Long missionId;
    private Double amount;
    private String transactionId;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String cardLastFour;
    private String message;
}
