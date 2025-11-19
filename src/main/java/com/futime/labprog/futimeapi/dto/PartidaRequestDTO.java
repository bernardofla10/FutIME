package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.FaseCompeticao;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record PartidaRequestDTO(
        @NotNull(message = "O mandante é obrigatório") Integer mandanteId,

        @NotNull(message = "O visitante é obrigatório") Integer visitanteId,

        @NotNull(message = "O estádio é obrigatório") Integer estadioId,

        @NotNull(message = "A fase da competição é obrigatória") FaseCompeticao fase,

        int golsMandante,
        int golsVisitante,

        @NotNull(message = "A data e hora da partida são obrigatórias") LocalDateTime dataHora) {
}
