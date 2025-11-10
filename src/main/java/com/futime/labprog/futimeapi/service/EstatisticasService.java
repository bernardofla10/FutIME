package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.EstatisticasRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticasResponseDTO;

import java.util.List;
import java.util.Optional;

public interface EstatisticasService {

    /**
     * Cria ou atualiza um registro de estatística para um jogador em uma competição.
     * Se já existir um registro para esta combinação, ele é atualizado.
     * Se não, um novo é criado.
     * @param jogadorId O ID do Jogador
     * @param competicaoId O ID da Competição
     * @param requestDTO O DTO com os dados (gols, assistencias, jogos)
     * @return O DTO da estatística salva.
     */
    EstatisticasResponseDTO salvarEstatisticas(Integer jogadorId, Integer competicaoId, EstatisticasRequestDTO requestDTO);

    /**
     * Lista todas as estatísticas registradas para um jogador específico.
     * @param jogadorId O ID do Jogador
     * @return Uma lista de DTOs de estatísticas.
     */
    List<EstatisticasResponseDTO> listarEstatisticasPorJogador(Integer jogadorId);
    
    /**
     * Busca um registro de estatística específico pelo seu ID único.
     * @param estatisticaId O ID do registro de estatística.
     * @return Um Optional com o DTO, se encontrado.
     */
    Optional<EstatisticasResponseDTO> buscarEstatisticaPorId(Integer estatisticaId);
    
    /**
     * Deleta um registro de estatística pelo seu ID único.
     * @param estatisticaId O ID do registro de estatística.
     * @return true se deletado, false se não encontrado.
     */
    boolean deletarEstatistica(Integer estatisticaId);
}