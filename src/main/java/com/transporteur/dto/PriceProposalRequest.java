package com.transporteur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour proposer un prix pour une mission (Transporteur → Client)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceProposalRequest {
    private Double proposedPrice;
}
