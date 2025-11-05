package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.TipoCompeticao;
import java.time.LocalDateTime;
import java.util.List;

public record CompeticaoResponseDTO(
    Integer id,
    String nome,
    String pais,
    String continente,
    TipoCompeticao tipoCompeticao,
    String temporada,
    List<ClubeResponseDTO> clubes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
