package com.futime.labprog.futimeapi.dto;

import com.futime.labprog.futimeapi.model.TipoCompeticao;
import java.util.List;

public record CompeticaoRequestDTO(
    String nome,
    String pais,
    String continente,
    TipoCompeticao tipoCompeticao,
    String temporada,
    List<Integer> clubeIds
) {
}
