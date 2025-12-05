package com.transporteur.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporteur.dto.AccountResponse;
import com.transporteur.dto.PlatformStatisticsResponse;
import com.transporteur.dto.TransactionResponse;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.model.Mission;
import com.transporteur.model.Payment;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.MissionRepository;
import com.transporteur.repository.PaymentRepository;
import com.transporteur.repository.TransporteurRepository;

/**
 * Service pour les opérations admin
 */
@Service
@Transactional(readOnly = true)
public class AdminService {

    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;
    private final TransporteurRepository transporteurRepository;
    private final PaymentRepository paymentRepository;
    private final MissionRepository missionRepository;

    public AdminService(
            CompteRepository compteRepository,
            ClientRepository clientRepository,
            TransporteurRepository transporteurRepository,
            PaymentRepository paymentRepository,
            MissionRepository missionRepository) {
        this.compteRepository = compteRepository;
        this.clientRepository = clientRepository;
        this.transporteurRepository = transporteurRepository;
        this.paymentRepository = paymentRepository;
        this.missionRepository = missionRepository;
    }

    /**
     * Récupérer tous les comptes avec détails
     */
    public List<AccountResponse> getAllAccounts() {
        List<Compte> comptes = compteRepository.findAll();
        List<AccountResponse> responses = new ArrayList<>();

        for (Compte compte : comptes) {
            AccountResponse response = new AccountResponse();
            response.setId(compte.getId());
            response.setEmail(compte.getEmail());
            response.setRole(compte.getRole().name());
            response.setDateCreation(compte.getDateCreation());

            // Get profile details based on role
            if (compte.getRole() == Compte.Role.CLIENT) {
                clientRepository.findByCompte(compte).ifPresent(client -> {
                    response.setProfileId(client.getIdClient());
                    response.setNom(client.getNom());
                    response.setPrenom(client.getPrenom());
                    response.setTelephone(client.getTelephone());
                    response.setAdresse(client.getAdresse());
                });
            } else if (compte.getRole() == Compte.Role.TRANSPORTEUR) {
                transporteurRepository.findByCompte(compte).ifPresent(transporteur -> {
                    response.setProfileId(transporteur.getIdTransporteur());
                    response.setNom(transporteur.getNom());
                    response.setPrenom(transporteur.getPrenom());
                    response.setTelephone(transporteur.getTelephone());
                    response.setAdresse(transporteur.getLocalisation());
                    // Note: typeVehicule and immatriculation not in current Transporteur model
                });
            }

            responses.add(response);
        }

        return responses;
    }

    /**
     * Récupérer toutes les transactions
     */
    public List<TransactionResponse> getAllTransactions() {
        List<Payment> payments = paymentRepository.findAll();
        List<TransactionResponse> responses = new ArrayList<>();

        for (Payment payment : payments) {
            TransactionResponse response = new TransactionResponse();
            response.setPaymentId(payment.getIdPayment());
            response.setMissionId(payment.getMission().getIdMission());
            response.setMissionLieuDepart(payment.getMission().getLieuDepart());
            response.setMissionLieuArrivee(payment.getMission().getLieuArrivee());
            response.setMissionDate(payment.getMission().getDateMission());

            // Client info
            response.setClientId(payment.getClient().getIdClient());
            response.setClientNom(payment.getClient().getNom());
            response.setClientPrenom(payment.getClient().getPrenom());
            response.setClientEmail(payment.getClient().getCompte().getEmail());

            // Transporteur info
            response.setTransporteurId(payment.getTransporteur().getIdTransporteur());
            response.setTransporteurNom(payment.getTransporteur().getNom());
            response.setTransporteurPrenom(payment.getTransporteur().getPrenom());
            response.setTransporteurEmail(payment.getTransporteur().getCompte().getEmail());

            // Payment info
            response.setAmount(payment.getAmount());
            response.setCardLastFour(payment.getCardLastFour());
            response.setTransactionId(payment.getTransactionId());
            response.setPaymentStatus(payment.getPaymentStatus());
            response.setPaymentDate(payment.getPaymentDate());

            responses.add(response);
        }

        return responses;
    }

    /**
     * Récupérer les statistiques de la plateforme
     */
    public PlatformStatisticsResponse getPlatformStatistics() {
        PlatformStatisticsResponse stats = new PlatformStatisticsResponse();

        // User statistics
        stats.setTotalAccounts(compteRepository.count());
        stats.setTotalClients(clientRepository.count());
        stats.setTotalTransporteurs(transporteurRepository.count());
        stats.setTotalAdmins(compteRepository.countByRole(Compte.Role.ADMIN));

        // Mission statistics
        List<Mission> allMissions = missionRepository.findAll();
        stats.setTotalMissions((long) allMissions.size());
        stats.setMissionsEnAttente(countMissionsByStatus(allMissions, Mission.StatutMission.EN_ATTENTE));
        stats.setMissionsPrixPropose(countMissionsByStatus(allMissions, Mission.StatutMission.PRIX_PROPOSE));
        stats.setMissionsPrixConfirme(countMissionsByStatus(allMissions, Mission.StatutMission.PRIX_CONFIRME));
        stats.setMissionsAcceptees(countMissionsByStatus(allMissions, Mission.StatutMission.ACCEPTEE));
        stats.setMissionsEnCours(countMissionsByStatus(allMissions, Mission.StatutMission.EN_COURS));
        stats.setMissionsTerminees(countMissionsByStatus(allMissions, Mission.StatutMission.TERMINEE));
        stats.setMissionsAnnulees(countMissionsByStatus(allMissions, Mission.StatutMission.ANNULEE));

        // Payment statistics
        List<Payment> allPayments = paymentRepository.findAll();
        stats.setTotalPayments((long) allPayments.size());
        stats.setTotalRevenue(allPayments.stream().mapToDouble(Payment::getAmount).sum());
        stats.setAverageTransactionAmount(
            allPayments.isEmpty() ? 0.0 : stats.getTotalRevenue() / allPayments.size()
        );
        stats.setPaidMissions(allMissions.stream().filter(m -> Boolean.TRUE.equals(m.getIsPaid())).count());
        stats.setUnpaidMissions(allMissions.stream().filter(m -> !Boolean.TRUE.equals(m.getIsPaid())).count());

        // Recent activity - Today
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);
        
        stats.setMissionsToday(countMissionsInPeriod(allMissions, startOfToday, endOfToday));
        stats.setPaymentsToday(countPaymentsInPeriod(allPayments, startOfToday, endOfToday));
        stats.setRevenueToday(calculateRevenueInPeriod(allPayments, startOfToday, endOfToday));

        // Recent activity - This Week
        LocalDateTime startOfWeek = LocalDate.now().minusDays(7).atStartOfDay();
        
        stats.setMissionsThisWeek(countMissionsInPeriod(allMissions, startOfWeek, endOfToday));
        stats.setPaymentsThisWeek(countPaymentsInPeriod(allPayments, startOfWeek, endOfToday));
        stats.setRevenueThisWeek(calculateRevenueInPeriod(allPayments, startOfWeek, endOfToday));

        // Recent activity - This Month
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        
        stats.setMissionsThisMonth(countMissionsInPeriod(allMissions, startOfMonth, endOfToday));
        stats.setPaymentsThisMonth(countPaymentsInPeriod(allPayments, startOfMonth, endOfToday));
        stats.setRevenueThisMonth(calculateRevenueInPeriod(allPayments, startOfMonth, endOfToday));

        return stats;
    }

    // Helper methods
    private Long countMissionsByStatus(List<Mission> missions, Mission.StatutMission status) {
        return missions.stream()
                .filter(m -> m.getStatut() == status)
                .count();
    }

    private Long countMissionsInPeriod(List<Mission> missions, LocalDateTime start, LocalDateTime end) {
        return missions.stream()
                .filter(m -> m.getDateCreation() != null)
                .filter(m -> !m.getDateCreation().isBefore(start) && !m.getDateCreation().isAfter(end))
                .count();
    }

    private Long countPaymentsInPeriod(List<Payment> payments, LocalDateTime start, LocalDateTime end) {
        return payments.stream()
                .filter(p -> p.getPaymentDate() != null)
                .filter(p -> !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
                .count();
    }

    private Double calculateRevenueInPeriod(List<Payment> payments, LocalDateTime start, LocalDateTime end) {
        return payments.stream()
                .filter(p -> p.getPaymentDate() != null)
                .filter(p -> !p.getPaymentDate().isBefore(start) && !p.getPaymentDate().isAfter(end))
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}
