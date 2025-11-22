package com.transporteur.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le rôle est obligatoire")
    @Pattern(regexp = "CLIENT|TRANSPORTEUR", message = "Le rôle doit être CLIENT ou TRANSPORTEUR")
    private String role;

    // Champs spécifiques pour Client
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String ville;

    // Champs spécifiques pour Transporteur
    private String localisation;

    // Constructeurs, getters et setters
    public RegisterRequest() {}

    // Getters et setters pour tous les champs
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
}