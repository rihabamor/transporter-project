package com.transporteur.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mission")
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mission")
    private Long idMission;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "transporteur_id", nullable = false)
    private Transporteur transporteur;

    @Column(nullable = false)
    private LocalDateTime dateMission;

    @Column(nullable = false)
    private String lieuDepart;

    @Column(nullable = false)
    private String lieuArrivee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutMission statut = StatutMission.EN_ATTENTE;

    private LocalDateTime dateCreation = LocalDateTime.now();

    private String description;

    // Payment related fields
    @Column(name = "proposed_price")
    private Double proposedPrice;

    @Column(name = "price_confirmed")
    private Boolean priceConfirmed = false;

    @Column(name = "is_paid")
    private Boolean isPaid = false;

    public enum StatutMission {
        EN_ATTENTE,         // Waiting for price proposal from transporteur
        PRIX_PROPOSE,       // Price proposed by transporteur, waiting for client confirmation
        PRIX_CONFIRME,      // Price confirmed by client, waiting for payment
        ACCEPTEE,           // Payment done, mission accepted
        EN_COURS,           // In progress
        TERMINEE,           // Completed
        ANNULEE             // Cancelled
    }
}
