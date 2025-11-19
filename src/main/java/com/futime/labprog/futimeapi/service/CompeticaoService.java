package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.CompeticaoRequestDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoResponseDTO;

import java.util.List;

public interface CompeticaoService {
    List<CompeticaoResponseDTO> listarCompeticoes();

    CompeticaoResponseDTO buscarCompeticaoPorId(Integer id);

    CompeticaoResponseDTO criarCompeticao(CompeticaoRequestDTO competicaoDTO);

    CompeticaoResponseDTO atualizarCompeticao(Integer id, CompeticaoRequestDTO competicaoDTO);

    void deletarCompeticao(Integer id);
}
