package com.transporteur.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity pour gérer les paiements des missions
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment")
    private Long idPayment;

    @ManyToOne
    @JoinColumn(name = "mission_id", nullable = false, unique = true)
    private Mission mission;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "transporteur_id", nullable = false)
    private Transporteur transporteur;

    @Column(nullable = false)
    private Double amount;

    // Card information (stored for reference - in production should be tokenized/encrypted)
    @Column(name = "card_last_four")
    private String cardLastFour;

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "transaction_id")
    private String transactionId; // For payment gateway reference

    @Column(name = "payment_status")
    private String paymentStatus = "COMPLETED"; // COMPLETED, PENDING, FAILED

    private LocalDateTime dateCreation = LocalDateTime.now();
}
