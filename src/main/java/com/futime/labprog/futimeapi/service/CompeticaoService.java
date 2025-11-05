package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.CompeticaoRequestDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoResponseDTO;

import java.util.List;
import java.util.Optional;

public interface CompeticaoService {
    List<CompeticaoResponseDTO> listarCompeticoes();
    Optional<CompeticaoResponseDTO> buscarCompeticaoPorId(Integer id);
    CompeticaoResponseDTO criarCompeticao(CompeticaoRequestDTO competicaoDTO);
    Optional<CompeticaoResponseDTO> atualizarCompeticao(Integer id, CompeticaoRequestDTO competicaoDTO);
    boolean deletarCompeticao(Integer id);
}
