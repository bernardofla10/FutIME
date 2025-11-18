package com.futime.labprog.futimeapi.dto;

public record EstatisticaPartidaRequestDTO(
    int minutosJogados,
    boolean cartaoAmarelo,
    boolean cartaoVermelho,
    boolean titular,
    int gols,
    int assistencias,
    Integer defesa,
    int finalizacoes,
    int chutesAGol,
    int desarmes
) {}


