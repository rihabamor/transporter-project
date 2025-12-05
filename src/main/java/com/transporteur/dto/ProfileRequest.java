package com.transporteur.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class ProfileRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;
    
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;
    
    // Champs spécifiques au client
    private String adresse;
    private String ville;
    
    // Champs spécifiques au transporteur
    private String localisation;
    
    @Email(message = "Format d'email invalide")
    private String email;

}
