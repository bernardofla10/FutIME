package com.futime.labprog.futimeapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (record) para criar ou atualizar um Clube.
 * Note que para associar um estádio, recebemos apenas o 'estadioId'.
 * O Service será responsável por buscar a entidade Estadio correspondente.
 */
public record ClubeRequestDTO(
        @NotBlank(message = "O nome do clube é obrigatório") String nome,

        @NotBlank(message = "A sigla é obrigatória") @Size(min = 2, max = 5, message = "A sigla deve ter entre 2 e 5 caracteres") String sigla,

        @NotBlank(message = "A cidade é obrigatória") String cidade,

        @NotBlank(message = "O país é obrigatório") String pais,

        @NotNull(message = "O ID do estádio é obrigatório") Integer estadioId // A "comanda" só precisa do ID do estádio
) {
}