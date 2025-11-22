package com.transporteur.controller;

import com.transporteur.model.Transporteur;
import com.transporteur.service.TransporteurService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transporteurs")
public class TransporteurController {

    private final TransporteurService transporteurService;

    public TransporteurController(TransporteurService transporteurService) {
        this.transporteurService = transporteurService;
    }

    @PostMapping
    public Transporteur createTransporteur(@RequestBody Transporteur t) {
        return transporteurService.addTransporteur(t);
    }

    @GetMapping
    public List<Transporteur> getAllTransporteurs() {
        return transporteurService.getAllTransporteurs();
    }
}
