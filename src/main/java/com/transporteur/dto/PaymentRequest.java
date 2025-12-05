package com.transporteur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour effectuer un paiement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    private Long missionId;
    
    // Card details
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    
    private Double amount;
}
