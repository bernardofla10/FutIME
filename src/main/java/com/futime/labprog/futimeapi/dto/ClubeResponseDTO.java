package com.futime.labprog.futimeapi.dto;

/**
 * DTO (record) para exibir um Clube.
 * Este é o "prato pronto" rico: ele inclui o DTO do estádio aninhado,
 * reutilizando o EstadioResponseDTO que já criamos.
 */
public record ClubeResponseDTO(
    Integer id,
    String nome,
    String sigla,
    String cidade,
    String pais,
    EstadioResponseDTO estadio // O "prato pronto" inclui o prato do estádio
) {}