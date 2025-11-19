package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.TipoCompeticao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CompeticaoRequestDTO(
        @NotBlank(message = "O nome da competição é obrigatório") String nome,

        @NotBlank(message = "O país é obrigatório") String pais,

        @NotBlank(message = "O continente é obrigatório") String continente,

        @NotNull(message = "O tipo da competição é obrigatório") TipoCompeticao tipoCompeticao,

        @NotBlank(message = "A temporada é obrigatória") String temporada,

        List<Integer> clubeIds) {
}
