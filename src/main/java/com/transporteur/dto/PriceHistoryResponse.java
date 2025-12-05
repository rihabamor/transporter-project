package com.transporteur.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'historique des changements de prix
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {
    private Long id;
    private Double oldPrice;
    private Double newPrice;
    private String changeReason;
    private String changedBy;
    private LocalDateTime changeDate;
}
