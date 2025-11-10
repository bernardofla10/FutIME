package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.TipoCompeticao; // ADICIONADO
import java.time.LocalDateTime; // ADICIONADO

/**
 * DTO para exibir um registro de EstatisticasJogadorCompeticao.
 * ATUALIZADO: Reflete os novos campos de Competicao.java
 */
public record EstatisticasResponseDTO(
    Integer id,
    int gols,
    int assistencias,
    int jogosDisputados,
    Integer jogadorId,
    String jogadorApelido,
    Integer competicaoId,
    String competicaoNome,
    String competicaoTemporada,
    TipoCompeticao tipoCompeticao, // ADICIONADO
    LocalDateTime competicaoCreatedAt // ADICIONADO
) {}