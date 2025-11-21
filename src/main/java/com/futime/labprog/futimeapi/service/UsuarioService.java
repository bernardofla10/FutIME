package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.LoginDTO;
import com.futime.labprog.futimeapi.dto.RegisterDTO;
import com.futime.labprog.futimeapi.dto.UsuarioResponseDTO;

public interface UsuarioService {
    UsuarioResponseDTO registerUser(RegisterDTO registerDTO);

    UsuarioResponseDTO login(LoginDTO loginDTO);

    UsuarioResponseDTO definirClubeFavorito(Integer usuarioId, Integer clubeId);

    UsuarioResponseDTO adicionarJogadorObservado(Integer usuarioId, Integer jogadorId);

    UsuarioResponseDTO buscarPerfil(Integer usuarioId);

    UsuarioResponseDTO removerJogadorObservado(Integer usuarioId, Integer jogadorId);
}
