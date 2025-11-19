package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.JogadorRequestDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;

import java.util.List;

public interface JogadorService {
    List<JogadorResponseDTO> listarJogadores();

    JogadorResponseDTO buscarJogadorPorId(Integer id);

    JogadorResponseDTO criarJogador(JogadorRequestDTO jogadorDTO);

    JogadorResponseDTO atualizarJogador(Integer id, JogadorRequestDTO jogadorDTO);

    void deletarJogador(Integer id);

    // Helper exposto para outros serviços (como UsuarioService)
    JogadorResponseDTO toResponseDTO(com.futime.labprog.futimeapi.model.Jogador jogador);
}
