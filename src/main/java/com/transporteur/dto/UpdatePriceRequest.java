package com.transporteur.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour modifier le prix proposé (avant confirmation client)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePriceRequest {
    
    @NotNull(message = "Le nouveau prix est requis")
    @Positive(message = "Le prix doit être positif")
    private Double newPrice;
    
    private String reason; // Raison du changement (optionnelle)
}
