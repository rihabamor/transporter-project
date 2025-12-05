package com.transporteur.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporteur.model.Compte;
import com.transporteur.model.Transporteur;
public interface TransporteurRepository extends JpaRepository<Transporteur, Long>{
    // Ajoutez cette méthode
    Optional<Transporteur> findByCompte(Compte compte);
    
    // Optionnel : si vous voulez aussi chercher par l'ID du compte
    Optional<Transporteur> findByCompteId(Long compteId);

}
