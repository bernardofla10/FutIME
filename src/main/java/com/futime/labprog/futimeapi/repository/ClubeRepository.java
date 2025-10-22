package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Clube;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubeRepository extends JpaRepository<Clube, Integer> {
    // Por enquanto, não precisamos de métodos customizados.
    // Os métodos CRUD (save, findById, findAll, deleteById)
    // são herdados do JpaRepository.
}