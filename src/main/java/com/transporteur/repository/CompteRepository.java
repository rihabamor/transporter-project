package com.transporteur.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.transporteur.model.Compte;
public interface CompteRepository extends JpaRepository<Compte, Long>{

    Optional<Compte> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Compte.Role role);

}
