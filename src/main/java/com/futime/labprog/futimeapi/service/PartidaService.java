package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.PartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.PartidaResponseDTO;
import java.util.List;

public interface PartidaService {
    List<PartidaResponseDTO> listarPartidas();

    PartidaResponseDTO buscarPorId(Integer id);

    PartidaResponseDTO criarPartida(PartidaRequestDTO dto);

    PartidaResponseDTO atualizarPartida(Integer id, PartidaRequestDTO dto);

    void deletarPartida(Integer id);
}
