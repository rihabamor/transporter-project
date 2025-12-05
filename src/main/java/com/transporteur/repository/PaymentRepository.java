package com.transporteur.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.transporteur.model.Mission;
import com.transporteur.model.Payment;

/**
 * Repository pour la gestion des paiements
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * Trouver un paiement par mission
     */
    Optional<Payment> findByMission(Mission mission);
    
    /**
     * Vérifier si une mission est payée
     */
    boolean existsByMission(Mission mission);
}
