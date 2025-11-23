package com.futime.labprog.futimeapi.dto;

import java.time.LocalDate;
import java.util.List;

public record JogadorResponseDTO(
        Integer id,
        String nomeCompleto,
        String apelido,
        LocalDate dataNascimento,
        String posicao,
        Double valorDeMercado,
        String imageUrl,
        ClubeResponseDTO clube,
        int golsTotais,
        int assistenciasTotais,
        List<EstatisticaTemporadaDTO> estatisticasPorTemporada,
        List<EstatisticaCompeticaoDTO> estatisticasPorCompeticao) {
}
