package com.transporteur.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transporteur.dto.AccountResponse;
import com.transporteur.dto.PlatformStatisticsResponse;
import com.transporteur.dto.TransactionResponse;
import com.transporteur.service.AdminService;

/**
 * Contrôleur pour les opérations admin
 * Tous les endpoints nécessitent le rôle ADMIN
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Récupérer tous les comptes (ADMIN uniquement)
     */
    @GetMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        try {
            List<AccountResponse> accounts = adminService.getAllAccounts();
            return ResponseEntity.ok(accounts);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer toutes les transactions (ADMIN uniquement)
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        try {
            List<TransactionResponse> transactions = adminService.getAllTransactions();
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Récupérer les statistiques de la plateforme (ADMIN uniquement)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlatformStatisticsResponse> getPlatformStatistics() {
        try {
            PlatformStatisticsResponse statistics = adminService.getPlatformStatistics();
            return ResponseEntity.ok(statistics);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
