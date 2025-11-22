package com.transporteur.service;
import java.util.List;

import org.springframework.stereotype.Service;

import com.transporteur.model.Client;
import com.transporteur.repository.ClientRepository;
@Service

public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client addClient(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

}
