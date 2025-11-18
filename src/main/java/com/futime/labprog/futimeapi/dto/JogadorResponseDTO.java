package com.futime.labprog.futimeapi.dto;

import java.time.LocalDate;
// import java.util.Set; // REMOVIDO
import java.util.List; // ADICIONADO

/**
 * DTO para exibir um Jogador (o "prato pronto" completo).
 * ATUALIZADO: Usa List em vez de Set
 */
public record JogadorResponseDTO(
    Integer id,
    String nomeCompleto,
    String apelido,
    LocalDate dataNascimento,
    String posicao,
    Double valorDeMercado,
    ClubeResponseDTO clube,
    
    Integer golsTotais,
    Integer assistenciasTotais,
    List<EstatisticaTemporadaDTO> estatisticasPorTemporada, // ATUALIZADO para List
    List<EstatisticaCompeticaoDTO> estatisticasPorCompeticao // ATUALIZADO para List
) {}