package com.transporteur.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.transporteur.dto.PaymentRequest;
import com.transporteur.dto.PaymentResponse;
import com.transporteur.dto.PaymentStatusResponse;
import com.transporteur.service.PaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Traiter un paiement pour une mission (CLIENT seulement)
     * POST /api/payment/process
     */
    @PostMapping("/process")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse response = paymentService.processPayment(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * Vérifier le statut de paiement d'une mission
     * GET /api/payment/status/{missionId}
     */
    @GetMapping("/status/{missionId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'TRANSPORTEUR')")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable Long missionId) {
        try {
            PaymentStatusResponse response = paymentService.checkPaymentStatus(missionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // Classe interne pour les réponses d'erreur
    private static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
