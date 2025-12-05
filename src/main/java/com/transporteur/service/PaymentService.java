package com.transporteur.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporteur.dto.PaymentRequest;
import com.transporteur.dto.PaymentResponse;
import com.transporteur.dto.PaymentStatusResponse;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.Payment;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.repository.PaymentRepository;

/**
 * Service pour gérer les paiements
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MissionRepository missionRepository;
    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;

    public PaymentService(PaymentRepository paymentRepository,
                         MissionRepository missionRepository,
                         ClientRepository clientRepository,
                         CompteRepository compteRepository) {
        this.paymentRepository = paymentRepository;
        this.missionRepository = missionRepository;
        this.clientRepository = clientRepository;
        this.compteRepository = compteRepository;
    }

    /**
     * Traiter un paiement
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // Récupérer le client connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));

        // Récupérer la mission
        Mission mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        // Vérifications
        if (!mission.getClient().getIdClient().equals(client.getIdClient())) {
            throw new RuntimeException("Vous n'êtes pas le client de cette mission");
        }

        if (mission.getStatut() != Mission.StatutMission.PRIX_CONFIRME) {
            throw new RuntimeException("Le prix n'a pas été confirmé pour cette mission");
        }

        if (mission.getIsPaid()) {
            throw new RuntimeException("Cette mission a déjà été payée");
        }

        if (!mission.getProposedPrice().equals(request.getAmount())) {
            throw new RuntimeException("Le montant ne correspond pas au prix proposé");
        }

        // Vérifier si un paiement existe déjà
        if (paymentRepository.existsByMission(mission)) {
            throw new RuntimeException("Un paiement existe déjà pour cette mission");
        }

        // Simuler le traitement du paiement (en production, utiliser une vraie gateway)
        String transactionId = generateTransactionId();
        String cardLastFour = request.getCardNumber().substring(request.getCardNumber().length() - 4);

        // Créer le paiement
        Payment payment = new Payment();
        payment.setMission(mission);
        payment.setClient(client);
        payment.setTransporteur(mission.getTransporteur());
        payment.setAmount(request.getAmount());
        payment.setCardLastFour(cardLastFour);
        payment.setCardHolderName(request.getCardHolderName());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setTransactionId(transactionId);
        payment.setPaymentStatus("COMPLETED");

        Payment savedPayment = paymentRepository.save(payment);

        // Mettre à jour la mission
        mission.setIsPaid(true);
        mission.setStatut(Mission.StatutMission.ACCEPTEE);
        missionRepository.save(mission);

        // Construire la réponse
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(savedPayment.getIdPayment());
        response.setMissionId(mission.getIdMission());
        response.setAmount(savedPayment.getAmount());
        response.setTransactionId(savedPayment.getTransactionId());
        response.setPaymentStatus(savedPayment.getPaymentStatus());
        response.setPaymentDate(savedPayment.getPaymentDate());
        response.setCardLastFour(savedPayment.getCardLastFour());
        response.setMessage("Paiement effectué avec succès");

        return response;
    }

    /**
     * Vérifier le statut de paiement d'une mission
     */
    public PaymentStatusResponse checkPaymentStatus(Long missionId) {
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("Mission non trouvée"));

        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setMissionId(missionId);
        response.setIsPaid(mission.getIsPaid());

        if (mission.getIsPaid()) {
            Payment payment = paymentRepository.findByMission(mission)
                    .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
            
            response.setAmount(payment.getAmount());
            response.setPaymentStatus(payment.getPaymentStatus());
            response.setMessage("Mission payée");
        } else {
            response.setAmount(mission.getProposedPrice());
            response.setPaymentStatus("PENDING");
            response.setMessage("Mission non payée");
        }

        return response;
    }

    /**
     * Générer un ID de transaction unique (simulation)
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
