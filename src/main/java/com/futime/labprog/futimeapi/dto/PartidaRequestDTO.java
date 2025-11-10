package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.FaseCompeticao;
import java.time.LocalDateTime;

public record PartidaRequestDTO(
    Integer mandanteId,
    Integer visitanteId,
    Integer estadioId,
    FaseCompeticao fase,
    int golsMandante,
    int golsVisitante,
    LocalDateTime dataHora
) {}


