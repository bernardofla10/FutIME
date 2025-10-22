package com.futime.labprog.futimeapi.dto;

import java.time.LocalDate;

/**
 * DTO para exibir um Jogador.
 * Inclui o ClubeResponseDTO aninhado.
 */
public record JogadorResponseDTO(
    Integer id,
    String nomeCompleto,
    String apelido,
    LocalDate dataNascimento,
    String posicao,
    ClubeResponseDTO clube // DTO do clube aninhado
) {}
