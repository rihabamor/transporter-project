package com.transporteur.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporteur.dto.MissionRequest;
import com.transporteur.dto.MissionResponse;
import com.transporteur.dto.PriceHistoryResponse;
import com.transporteur.dto.TransporteurAvailableResponse;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.PriceHistory;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.repository.PriceHistoryRepository;
import com.transporteur.repository.TransporteurRepository;

@Service
public class MissionService {

    private final MissionRepository missionRepository;
    private final ClientRepository clientRepository;
    private final TransporteurRepository transporteurRepository;
    private final CompteRepository compteRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    public MissionService(MissionRepository missionRepository, 
                         ClientRepository clientRepository,
                         TransporteurRepository transporteurRepository,
                         CompteRepository compteRepository,
                         PriceHistoryRepository priceHistoryRepository) {
        this.missionRepository = missionRepository;
        this.clientRepository = clientRepository;
        this.transporteurRepository = transporteurRepository;
        this.compteRepository = compteRepository;
        this.priceHistoryRepository = priceHistoryRepository;
    }

    /**
     * Obtenir la liste des transporteurs disponibles
     */
    public List<TransporteurAvailableResponse> getAvailableTransporteurs() {
        List<Transporteur> transporteurs = transporteurRepository.findAll();
        
        return transporteurs.stream()
            .filter(t -> t.getDisponible() != null && t.getDisponible())
            .map(this::mapToTransporteurAvailableResponse)
            .collect(Collectors.toList());
    }

    /**
     * Créer une nouvelle mission (CLIENT seulement)
     */
    @Transactional
    public MissionResponse createMission(MissionRequest request) {
        // Récupérer le client connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));

        // Récupérer le transporteur sélectionné
        Transporteur transporteur = transporteurRepository.findById(request.getTransporteurId())
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé"));

        // Vérifier que le transporteur est disponible
        if (transporteur.getDisponible() == null || !transporteur.getDisponible()) {
            throw new RuntimeException("Ce transporteur n'est pas disponible");
        }

        // Créer la mission
        Mission mission = new Mission();
        mission.setClient(client);
        mission.setTransporteur(transporteur);
        mission.setDateMission(request.getDateMission());
        mission.setLieuDepart(request.getLieuDepart());
        mission.setLieuArrivee(request.getLieuArrivee());
        mission.setDescription(request.getDescription());
        mission.setStatut(Mission.StatutMission.EN_ATTENTE);
        mission.setDateCreation(LocalDateTime.now());

        Mission savedMission = missionRepository.save(mission);

        return mapToMissionResponse(savedMission);
    }

    /**
     * Obtenir toutes les missions d'un client
     */
    public List<MissionResponse> getClientMissions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));

        List<Mission> missions = missionRepository.findByClient(client);
        return missions.stream()
                .map(this::mapToMissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir toutes les missions d'un transporteur
     */
    public List<MissionResponse> getTransporteurMissions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Transporteur transporteur = transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));

        List<Mission> missions = missionRepository.findByTransporteur(transporteur);
        return missions.stream()
                .map(this::mapToMissionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir une mission par ID
     */
    public MissionResponse getMissionById(Long id) {
        Mission mission = missionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifier que l'utilisateur a accès à cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        boolean isClient = mission.getClient().getCompte().getId().equals(compte.getId());
        boolean isTransporteur = mission.getTransporteur().getCompte().getId().equals(compte.getId());

        if (!isClient && !isTransporteur) {
            throw new RuntimeException("Vous n'avez pas accès à cette mission");
        }

        return mapToMissionResponse(mission);
    }

    /**
     * Obtenir le numéro de téléphone du transporteur d'une mission (CLIENT seulement)
     */
    public Map<String, String> getTransporteurContact(Long missionId) {
        // 1. Trouver la mission par ID
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // 2. Vérifier que le client connecté est le propriétaire de cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));

        if (!mission.getClient().getIdClient().equals(client.getIdClient())) {
            throw new RuntimeException("Vous n'avez pas accès à cette mission");
        }

        // 3. Obtenir le transporteur de la mission
        Transporteur transporteur = mission.getTransporteur();
        if (transporteur == null) {
            throw new RuntimeException("Aucun transporteur assigné à cette mission");
        }

        // 4. Retourner le numéro de téléphone
        Map<String, String> response = new HashMap<>();
        response.put("telephone", transporteur.getTelephone());
        response.put("nom", transporteur.getNom());
        response.put("prenom", transporteur.getPrenom());
        
        return response;
    }

    /**
     * Mettre à jour le statut d'une mission (TRANSPORTEUR seulement)
     */
    @Transactional
    public MissionResponse updateMissionStatus(Long missionId, String newStatus) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifier que c'est le transporteur de cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Transporteur transporteur = transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));

        if (!mission.getTransporteur().getIdTransporteur().equals(transporteur.getIdTransporteur())) {
            throw new RuntimeException("Vous n'êtes pas le transporteur de cette mission");
        }

        // Mettre à jour le statut
        try {
            Mission.StatutMission statutMission = Mission.StatutMission.valueOf(newStatus.toUpperCase());
            
            // Vérifier que la mission est payée avant de passer à EN_COURS
            if (statutMission == Mission.StatutMission.EN_COURS) {
                if (mission.getIsPaid() == null || !mission.getIsPaid()) {
                    throw new RuntimeException("La mission doit être payée avant de pouvoir commencer");
                }
            }
            
            mission.setStatut(statutMission);
            Mission updatedMission = missionRepository.save(mission);
            
            return mapToMissionResponse(updatedMission);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide: " + newStatus);
        }
    }

    /**
     * Annuler une mission (CLIENT seulement)
     */
    @Transactional
    public MissionResponse cancelMission(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifier que c'est le client de cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));

        if (!mission.getClient().getIdClient().equals(client.getIdClient())) {
            throw new RuntimeException("Vous n'êtes pas le client de cette mission");
        }

        // Ne peut annuler que si la mission n'est pas déjà terminée
        if (mission.getStatut() == Mission.StatutMission.TERMINEE) {
            throw new RuntimeException("Impossible d'annuler une mission terminée");
        }

        mission.setStatut(Mission.StatutMission.ANNULEE);
        
        Mission updatedMission = missionRepository.save(mission);
        return mapToMissionResponse(updatedMission);
    }

    /**
     * Proposer un prix pour une mission (TRANSPORTEUR seulement)
     */
    @Transactional
    public MissionResponse proposePrice(Long missionId, Double proposedPrice) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifier que c'est le transporteur de cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Transporteur transporteur = transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));

        if (!mission.getTransporteur().getIdTransporteur().equals(transporteur.getIdTransporteur())) {
            throw new RuntimeException("Vous n'êtes pas le transporteur de cette mission");
        }

        // Vérifier que la mission est en attente
        if (mission.getStatut() != Mission.StatutMission.EN_ATTENTE) {
            throw new RuntimeException("Impossible de proposer un prix pour cette mission");
        }

        if (proposedPrice == null || proposedPrice <= 0) {
            throw new RuntimeException("Prix invalide");
        }

        // Proposer le prix
        mission.setProposedPrice(proposedPrice);
        mission.setStatut(Mission.StatutMission.PRIX_PROPOSE);
        mission.setPriceConfirmed(false);

        Mission updatedMission = missionRepository.save(mission);
        return mapToMissionResponse(updatedMission);
    }

    /**
     * Confirmer le prix d'une mission (CLIENT seulement)
     */
    @Transactional
    public MissionResponse confirmPrice(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifier que c'est le client de cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));

        if (!mission.getClient().getIdClient().equals(client.getIdClient())) {
            throw new RuntimeException("Vous n'êtes pas le client de cette mission");
        }

        // Vérifier que la mission a un prix proposé
        if (mission.getStatut() != Mission.StatutMission.PRIX_PROPOSE) {
            throw new RuntimeException("Aucun prix proposé pour cette mission");
        }

        if (mission.getProposedPrice() == null) {
            throw new RuntimeException("Prix non défini");
        }

        // Confirmer le prix
        mission.setPriceConfirmed(true);
        mission.setStatut(Mission.StatutMission.PRIX_CONFIRME);

        Mission updatedMission = missionRepository.save(mission);
        return mapToMissionResponse(updatedMission);
    }

    /**
     * Modifier le prix proposé (TRANSPORTEUR seulement, avant confirmation du client)
     */
    @Transactional
    public MissionResponse updateProposedPrice(Long missionId, Double newPrice, String reason) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifier que c'est le transporteur de cette mission
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));

        Transporteur transporteur = transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));

        if (!mission.getTransporteur().getIdTransporteur().equals(transporteur.getIdTransporteur())) {
            throw new RuntimeException("Vous n'êtes pas le transporteur de cette mission");
        }

        // Vérifier que le prix n'a pas encore été confirmé par le client
        if (mission.getStatut() != Mission.StatutMission.PRIX_PROPOSE) {
            throw new RuntimeException("Le prix ne peut plus être modifié (statut actuel: " + mission.getStatut() + ")");
        }

        if (mission.getPriceConfirmed() != null && mission.getPriceConfirmed()) {
            throw new RuntimeException("Le prix a déjà été confirmé par le client");
        }

        if (newPrice == null || newPrice <= 0) {
            throw new RuntimeException("Prix invalide");
        }

        // Sauvegarder l'ancien prix dans l'historique
        PriceHistory history = new PriceHistory();
        history.setMission(mission);
        history.setOldPrice(mission.getProposedPrice());
        history.setNewPrice(newPrice);
        history.setChangeReason(reason != null ? reason : "Modification du prix par le transporteur");
        history.setChangedBy(email);
        history.setChangeDate(LocalDateTime.now());
        priceHistoryRepository.save(history);

        // Mettre à jour le prix
        mission.setProposedPrice(newPrice);
        
        Mission updatedMission = missionRepository.save(mission);
        return mapToMissionResponse(updatedMission);
    }

    // Méthodes de mapping privées
    private MissionResponse mapToMissionResponse(Mission mission) {
        MissionResponse response = new MissionResponse();
        response.setIdMission(mission.getIdMission());
        response.setClientId(mission.getClient().getIdClient());
        response.setClientNom(mission.getClient().getNom());
        response.setClientPrenom(mission.getClient().getPrenom());
        response.setTransporteurId(mission.getTransporteur().getIdTransporteur());
        response.setTransporteurNom(mission.getTransporteur().getNom());
        response.setTransporteurPrenom(mission.getTransporteur().getPrenom());
        response.setDateMission(mission.getDateMission());
        response.setLieuDepart(mission.getLieuDepart());
        response.setLieuArrivee(mission.getLieuArrivee());
        response.setStatut(mission.getStatut().name());
        response.setDateCreation(mission.getDateCreation());
        response.setDescription(mission.getDescription());
        
        // Champs de paiement
        response.setProposedPrice(mission.getProposedPrice());
        response.setPriceConfirmed(mission.getPriceConfirmed() != null ? mission.getPriceConfirmed() : false);
        response.setIsPaid(mission.getIsPaid() != null ? mission.getIsPaid() : false);
        
        // Historique des changements de prix
        List<PriceHistory> priceHistories = priceHistoryRepository.findByMissionOrderByChangeDateDesc(mission);
        List<PriceHistoryResponse> historyResponses = priceHistories.stream()
            .map(this::mapToPriceHistoryResponse)
            .collect(Collectors.toList());
        response.setPriceHistory(historyResponses);
        
        return response;
    }

    private PriceHistoryResponse mapToPriceHistoryResponse(PriceHistory history) {
        PriceHistoryResponse response = new PriceHistoryResponse();
        response.setId(history.getIdPriceHistory());
        response.setOldPrice(history.getOldPrice());
        response.setNewPrice(history.getNewPrice());
        response.setChangeReason(history.getChangeReason());
        response.setChangedBy(history.getChangedBy());
        response.setChangeDate(history.getChangeDate());
        return response;
    }

    private TransporteurAvailableResponse mapToTransporteurAvailableResponse(Transporteur transporteur) {
        TransporteurAvailableResponse response = new TransporteurAvailableResponse();
        response.setIdTransporteur(transporteur.getIdTransporteur());
        response.setNom(transporteur.getNom());
        response.setPrenom(transporteur.getPrenom());
        response.setTelephone(transporteur.getTelephone());
        response.setLocalisation(transporteur.getLocalisation());
        response.setNoteMoyenne(transporteur.getNoteMoyenne());
        response.setDisponible(transporteur.getDisponible());
        return response;
    }
}
