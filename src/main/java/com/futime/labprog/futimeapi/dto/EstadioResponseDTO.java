package com.futime.labprog.futimeapi.dto;

// DTO para exibir um Estádio.
// Usado para GET
public record EstadioResponseDTO(
    Integer id,
    String nome,
    String cidade,
    String pais
) {}