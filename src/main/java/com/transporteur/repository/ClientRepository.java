package com.transporteur.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporteur.model.Client;
import com.transporteur.model.Compte;
public interface ClientRepository extends JpaRepository<Client, Long>{

    // Ajoutez cette méthode
    Optional<Client> findByCompte(Compte compte);
    
    // Optionnel : si vous voulez aussi chercher par l'ID du compte
    Optional<Client> findByCompteId(Long compteId);

}
