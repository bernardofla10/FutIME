package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
}


