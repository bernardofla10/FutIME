package com.futime.labprog.futimeapi.dto;

import java.util.List;

public record UsuarioResponseDTO(
        Integer id,
        String nome,
        String email,
        ClubeResponseDTO clubeFavorito,
        List<JogadorResponseDTO> jogadoresObservados) {
}
