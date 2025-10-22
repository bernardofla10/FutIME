package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Jogador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Integer> {
    // Métodos CRUD básicos herdados
}