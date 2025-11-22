package com.transporteur.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.transporteur.model.Transporteur;
import com.transporteur.repository.TransporteurRepository;

@Service
public class TransporteurService {

    private final TransporteurRepository transporteurRepository;

    public TransporteurService(TransporteurRepository transporteurRepository) {
        this.transporteurRepository = transporteurRepository;
    }

    public Transporteur addTransporteur(Transporteur t) {
        return transporteurRepository.save(t);
    }

    public List<Transporteur> getAllTransporteurs() {
        return transporteurRepository.findAll();
    }

}
