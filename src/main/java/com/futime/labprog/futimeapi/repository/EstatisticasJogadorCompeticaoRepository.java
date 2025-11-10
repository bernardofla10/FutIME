package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.EstatisticasJogadorCompeticao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstatisticasJogadorCompeticaoRepository extends JpaRepository<EstatisticasJogadorCompeticao, Integer> {

    /**
     * Novo método customizado: Encontra todos os registros de estatísticas
     * para um jogador específico, usando o ID do jogador.
     */
    List<EstatisticasJogadorCompeticao> findByJogadorId(Integer jogadorId);

    /**
     * Novo método customizado: Encontra um registro de estatística específico
     * pela combinação de ID do jogador e ID da competição.
     * Isso é o coração da nossa lógica "Upsert".
     */
    Optional<EstatisticasJogadorCompeticao> findByJogadorIdAndCompeticaoId(Integer jogadorId, Integer competicaoId);
}