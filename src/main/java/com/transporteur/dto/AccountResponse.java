package com.transporteur.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour afficher la liste des comptes (Admin uniquement)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    
    private Long id;
    private String email;
    private String role;
    private Instant dateCreation;
    
    // Client/Transporteur details
    private Long profileId;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    
    // Transporteur specific
    private String typeVehicule;
    private String immatriculation;
}
