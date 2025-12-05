package com.transporteur.dto;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour le profil admin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {
    private Long accountId;
    private String email;
    private String role;
    private Instant dateCreation;
    private List<String> permissions;
}
