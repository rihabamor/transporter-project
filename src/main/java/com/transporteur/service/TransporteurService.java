package com.transporteur.service;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transporteur.dto.ProfileRequest;
import com.transporteur.model.Compte;
import com.transporteur.model.Transporteur;
import com.transporteur.repository.CompteRepository;
import com.transporteur.repository.TransporteurRepository;

@Service
public class TransporteurService {

    private final TransporteurRepository transporteurRepository;
    private final CompteRepository compteRepository;

    public TransporteurService(TransporteurRepository transporteurRepository, CompteRepository compteRepository) {
        this.transporteurRepository = transporteurRepository;
        this.compteRepository = compteRepository;
    }

    public Transporteur addTransporteur(Transporteur t) {
        return transporteurRepository.save(t);
    }

    public List<Transporteur> getAllTransporteurs() {
        return transporteurRepository.findAll();
    }

    public Transporteur getProfilTransporteur() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        return transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));
    }

    public Transporteur updateProfilTransporteur(ProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Transporteur transporteur = transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));
        
        // Mise à jour des informations
        transporteur.setNom(request.getNom());
        transporteur.setPrenom(request.getPrenom());
        transporteur.setTelephone(request.getTelephone());
        transporteur.setLocalisation(request.getLocalisation());
        
        // Mise à jour de l'email si fourni
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            compte.setEmail(request.getEmail());
            compteRepository.save(compte);
        }
        
        return transporteurRepository.save(transporteur);
    }

    @Transactional
    public Transporteur updateDisponibilite(Boolean disponible) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Transporteur transporteur = transporteurRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil transporteur non trouvé"));
        
        transporteur.setDisponible(disponible);
        Transporteur saved = transporteurRepository.save(transporteur);
        transporteurRepository.flush(); // Force immediate database write
        return saved;
    }

}
