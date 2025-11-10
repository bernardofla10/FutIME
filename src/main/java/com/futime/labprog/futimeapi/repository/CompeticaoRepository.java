
package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Competicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompeticaoRepository extends JpaRepository<Competicao, Integer> {
    List<Competicao> findByClubes_Id(Integer clubeId);
}