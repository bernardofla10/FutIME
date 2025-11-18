package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.EstatisticaPartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticaPartidaResponseDTO;
import java.util.List;
import java.util.Optional;

public interface EstatisticaPartidaService {
    EstatisticaPartidaResponseDTO salvar(Integer jogadorId, Integer partidaId, EstatisticaPartidaRequestDTO dto);
    List<EstatisticaPartidaResponseDTO> listarPorJogador(Integer jogadorId);
    Optional<EstatisticaPartidaResponseDTO> buscarPorId(Integer id);
    boolean deletar(Integer id);
}


