package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Competicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompeticaoRepository extends JpaRepository<Competicao, Integer> {
    // Por enquanto, não precisamos de métodos customizados.
    // Os métodos CRUD (save, findById, findAll, deleteById)
    // são herdados do JpaRepository.
}
