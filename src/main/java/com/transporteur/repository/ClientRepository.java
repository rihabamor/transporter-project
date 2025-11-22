package com.transporteur.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.transporteur.model.Client;
public interface ClientRepository extends JpaRepository<Client, Long>{

}
