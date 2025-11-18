package com.futime.labprog.futimeapi.dto;

// DTO para criar ou atualizar um Estádio.
// Não tem ID, pois o ID é gerado pelo banco (no create) ou vem pela URL (no update).
// Usado para POST e PUT.
public record EstadioRequestDTO(
    String nome,
    String cidade,
    String pais
) {}