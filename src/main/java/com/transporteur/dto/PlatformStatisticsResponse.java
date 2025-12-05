package com.transporteur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les statistiques de la plateforme (Admin uniquement)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatisticsResponse {
    
    // User statistics
    private Long totalAccounts;
    private Long totalClients;
    private Long totalTransporteurs;
    private Long totalAdmins;
    
    // Mission statistics
    private Long totalMissions;
    private Long missionsEnAttente;
    private Long missionsPrixPropose;
    private Long missionsPrixConfirme;
    private Long missionsAcceptees;
    private Long missionsEnCours;
    private Long missionsTerminees;
    private Long missionsAnnulees;
    
    // Payment statistics
    private Long totalPayments;
    private Double totalRevenue;
    private Double averageTransactionAmount;
    private Long paidMissions;
    private Long unpaidMissions;
    
    // Recent activity
    private Long missionsToday;
    private Long paymentsToday;
    private Double revenueToday;
    
    private Long missionsThisWeek;
    private Long paymentsThisWeek;
    private Double revenueThisWeek;
    
    private Long missionsThisMonth;
    private Long paymentsThisMonth;
    private Double revenueThisMonth;
}
