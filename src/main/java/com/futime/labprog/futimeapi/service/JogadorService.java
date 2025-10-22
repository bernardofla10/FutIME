package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.JogadorRequestDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;

import java.util.List;
import java.util.Optional;

public interface JogadorService {
    List<JogadorResponseDTO> listarJogadores();
    Optional<JogadorResponseDTO> buscarJogadorPorId(Integer id);
    JogadorResponseDTO criarJogador(JogadorRequestDTO jogadorDTO);
    Optional<JogadorResponseDTO> atualizarJogador(Integer id, JogadorRequestDTO jogadorDTO);
    boolean deletarJogador(Integer id);
}
