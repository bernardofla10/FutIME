package com.futime.labprog.futimeapi.dto;

/**
 * DTO de apoio para o TOTAL de estatísticas de um jogador em UMA temporada.
 */
public record EstatisticaTemporadaDTO(
    String temporada,
    int totalGols,
    int totalAssistencias
) {}