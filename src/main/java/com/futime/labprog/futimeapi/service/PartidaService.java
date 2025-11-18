package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.PartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.PartidaResponseDTO;
import java.util.List;
import java.util.Optional;

public interface PartidaService {
    List<PartidaResponseDTO> listarPartidas();
    Optional<PartidaResponseDTO> buscarPorId(Integer id);
    PartidaResponseDTO criarPartida(PartidaRequestDTO dto);
    Optional<PartidaResponseDTO> atualizarPartida(Integer id, PartidaRequestDTO dto);
    boolean deletarPartida(Integer id);
}


