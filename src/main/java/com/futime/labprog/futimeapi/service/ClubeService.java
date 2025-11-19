package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeRequestDTO;
import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;

import java.util.List;

public interface ClubeService {
    List<ClubeResponseDTO> listarClubes();

    ClubeResponseDTO buscarClubePorId(Integer id);

    ClubeResponseDTO criarClube(ClubeRequestDTO clubeDTO);

    ClubeResponseDTO atualizarClube(Integer id, ClubeRequestDTO clubeDTO);

    void deletarClube(Integer id);
}