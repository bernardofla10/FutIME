package com.futime.labprog.futimeapi.dto;

import java.time.LocalDateTime;

public record EstatisticaPartidaResponseDTO(
    Integer id,
    Integer jogadorId,
    String jogadorApelido,
    Integer partidaId,
    int minutosJogados,
    boolean cartaoAmarelo,
    boolean cartaoVermelho,
    boolean titular,
    int gols,
    int assistencias,
    Integer defesa,
    int finalizacoes,
    int chutesAGol,
    int desarmes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}


