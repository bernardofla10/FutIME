package com.futime.labprog.futimeapi.dto;

import java.time.LocalDate;

/**
 * DTO para criar ou atualizar um Jogador.
 * Recebe apenas o 'clubeId' para associação.
 */
public record JogadorRequestDTO(
    String nomeCompleto,
    String apelido,
    LocalDate dataNascimento,
    String posicao,
    Integer clubeId,
    Double valorDeMercado
) {}