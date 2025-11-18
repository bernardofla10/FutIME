package com.futime.labprog.futimeapi.dto;

/**
 * DTO (record) para criar ou atualizar um Clube.
 * Note que para associar um estádio, recebemos apenas o 'estadioId'.
 * O Service será responsável por buscar a entidade Estadio correspondente.
 */
public record ClubeRequestDTO(
    String nome,
    String sigla,
    String cidade,
    String pais,
    Integer estadioId // A "comanda" só precisa do ID do estádio
) {}