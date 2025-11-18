package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.FaseCompeticao;
import java.time.LocalDateTime;

public record PartidaResponseDTO(
    Integer id,
    ClubeResponseDTO mandante,
    ClubeResponseDTO visitante,
    EstadioResponseDTO estadio,
    FaseCompeticao fase,
    int golsMandante,
    int golsVisitante,
    LocalDateTime dataHora,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}


