package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeRequestDTO;
import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;

import java.util.List;
import java.util.Optional;

public interface ClubeService {
    List<ClubeResponseDTO> listarClubes();
    Optional<ClubeResponseDTO> buscarClubePorId(Integer id);
    ClubeResponseDTO criarClube(ClubeRequestDTO clubeDTO);
    Optional<ClubeResponseDTO> atualizarClube(Integer id, ClubeRequestDTO clubeDTO);
    boolean deletarClube(Integer id);
}