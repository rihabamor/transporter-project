package com.transporteur.service;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.transporteur.dto.ProfileRequest;
import com.transporteur.model.Client;
import com.transporteur.model.Compte;
import com.transporteur.repository.ClientRepository;
import com.transporteur.repository.CompteRepository;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;

    public ClientService(ClientRepository clientRepository, CompteRepository compteRepository) {
        this.clientRepository = clientRepository;
        this.compteRepository = compteRepository;
    }

    public Client addClient(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getProfilClient() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        return clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));
    }

    public Client updateProfilClient(ProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Compte non trouvé"));
        
        Client client = clientRepository.findByCompte(compte)
                .orElseThrow(() -> new RuntimeException("Profil client non trouvé"));
        
        // Mise à jour des informations
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setTelephone(request.getTelephone());
        client.setAdresse(request.getAdresse());
        client.setVille(request.getVille());
        
        // Mise à jour de l'email si fourni
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            compte.setEmail(request.getEmail());
            compteRepository.save(compte);
        }
        
        return clientRepository.save(client);
    }

}
