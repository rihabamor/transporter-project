package com.transporteur.service;

import com.transporteur.model.Compte;
import com.transporteur.repository.CompteRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService{

    private final CompteRepository compteRepository;

    public CustomUserDetailsService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Compte compte = compteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        return new User(
                compte.getEmail(),
                compte.getPassword(),
                Collections.singletonList(() -> "ROLE_" + compte.getRole().name())
        );
    }

}
