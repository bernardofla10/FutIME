package com.futime.labprog.futimeapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;

/**
 * DTO para criar ou atualizar um Jogador.
 * Recebe apenas o 'clubeId' para associação.
 */
public record JogadorRequestDTO(
        @NotBlank(message = "O nome completo é obrigatório") String nomeCompleto,

        String apelido, // Opcional

        @NotNull(message = "A data de nascimento é obrigatória") @Past(message = "A data de nascimento deve ser no passado") LocalDate dataNascimento,

        @NotBlank(message = "A posição é obrigatória") String posicao,

        Integer clubeId, // Pode ser nulo (sem clube)

        @PositiveOrZero(message = "O valor de mercado não pode ser negativo") Double valorDeMercado) {
}