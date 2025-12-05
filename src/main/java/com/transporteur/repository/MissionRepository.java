package com.transporteur.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.transporteur.model.Client;
import com.transporteur.model.Mission;
import com.transporteur.model.Transporteur;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    // Trouver toutes les missions d'un client
    List<Mission> findByClient(Client client);

    // Trouver toutes les missions d'un transporteur
    List<Mission> findByTransporteur(Transporteur transporteur);

    // Trouver les missions par statut
    List<Mission> findByStatut(Mission.StatutMission statut);

    // Trouver les missions d'un client par statut
    List<Mission> findByClientAndStatut(Client client, Mission.StatutMission statut);

    // Trouver les missions d'un transporteur par statut
    List<Mission> findByTransporteurAndStatut(Transporteur transporteur, Mission.StatutMission statut);

    // Compter les missions terminées d'un client
    long countByClientAndStatut(Client client, Mission.StatutMission statut);

    // Compter les missions terminées d'un transporteur
    long countByTransporteurAndStatut(Transporteur transporteur, Mission.StatutMission statut);

    // Trouver les missions en cours d'un client
    @Query("SELECT COUNT(m) FROM Mission m WHERE m.client = :client AND m.statut IN ('EN_COURS', 'ACCEPTEE')")
    long countActiveMissionsByClient(@Param("client") Client client);

    // Trouver les missions en cours d'un transporteur
    @Query("SELECT COUNT(m) FROM Mission m WHERE m.transporteur = :transporteur AND m.statut IN ('EN_COURS', 'ACCEPTEE')")
    long countActiveMissionsByTransporteur(@Param("transporteur") Transporteur transporteur);
}
