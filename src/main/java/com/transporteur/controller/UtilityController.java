package com.transporteur.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TEMPORARY Controller pour générer le hash BCrypt du mot de passe admin
 * ⚠️ SUPPRIMER CE CONTROLLER EN PRODUCTION! ⚠️
 */
@RestController
@RequestMapping("/api/util")
@CrossOrigin(origins = "http://localhost:4200")
public class UtilityController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Générer un hash BCrypt pour un mot de passe
     * ⚠️ À utiliser uniquement en développement pour créer le mot de passe admin ⚠️
     */
    @PostMapping("/generate-password-hash")
    public ResponseEntity<String> generatePasswordHash(@RequestBody PasswordRequest request) {
        String hash = passwordEncoder.encode(request.getPassword());
        return ResponseEntity.ok(hash);
    }

    public static class PasswordRequest {
        private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
