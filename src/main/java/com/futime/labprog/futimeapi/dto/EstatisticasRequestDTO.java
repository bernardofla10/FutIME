package com.futime.labprog.futimeapi.dto;

/**
 * DTO para criar ou atualizar um registro de estatística.
 * Note que os IDs do Jogador e da Competição virão
 * pela URL (ex: POST /jogadores/1/competicoes/3/estatisticas),
 * então não precisamos deles no corpo.
 */
public record EstatisticasRequestDTO(
    int gols,
    int assistencias,
    int jogosDisputados
) {}