package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.TipoCompeticao; // ADICIONADO

/**
 * DTO de apoio para estatísticas de um jogador em UMA competição específica.
 * ATUALIZADO: Adiciona tipoCompeticao
 */
public record EstatisticaCompeticaoDTO(
    String nomeCompeticao,
    String temporada,
    TipoCompeticao tipoCompeticao, // ADICIONADO
    int gols,
    int assistencias
) {}