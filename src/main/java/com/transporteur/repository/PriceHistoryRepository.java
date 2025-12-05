package com.transporteur.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.transporteur.model.Mission;
import com.transporteur.model.PriceHistory;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    /**
     * Récupérer l'historique des prix d'une mission, trié par date décroissante
     */
    List<PriceHistory> findByMissionOrderByChangeDateDesc(Mission mission);
}
