package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.JogadorEstatisticaPartida;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JogadorEstatisticaPartidaRepository extends JpaRepository<JogadorEstatisticaPartida, Integer> {
    List<JogadorEstatisticaPartida> findByJogador_Id(Integer jogadorId);
    Optional<JogadorEstatisticaPartida> findByJogador_IdAndPartida_Id(Integer jogadorId, Integer partidaId);
}


