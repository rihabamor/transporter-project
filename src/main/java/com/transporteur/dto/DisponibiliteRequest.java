package com.transporteur.dto;

import jakarta.validation.constraints.NotNull;

public class DisponibiliteRequest {
    
    @NotNull(message = "Le statut de disponibilité est requis")
    private Boolean disponible;

    public DisponibiliteRequest() {
    }

    public DisponibiliteRequest(Boolean disponible) {
        this.disponible = disponible;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
}
