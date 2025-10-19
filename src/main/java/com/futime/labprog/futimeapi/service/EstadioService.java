package com.futime.labprog.futimeapi.service;

// Importa os DTOs. Note que ele NÃO conhece a Entidade "Estadio".
// Ele só fala a linguagem de DTOs, a "linguagem pública" da API.
import com.futime.labprog.futimeapi.dto.EstadioRequestDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import java.util.List;
import java.util.Optional;

public interface EstadioService {

    // Contrato 1: Deve existir um método "listarEstadios"
    // que não recebe nada e retorna uma LISTA de "pratos prontos" (ResponseDTO).
    List<EstadioResponseDTO> listarEstadios();

    // Contrato 2: Deve existir um método "buscarEstadioPorId"
    // que recebe um "id" e retorna um "Optional" (para evitar NullPointerException)
    // contendo o "prato pronto" (ResponseDTO), se ele for encontrado.
    Optional<EstadioResponseDTO> buscarEstadioPorId(Integer id);

    // Contrato 3: Deve existir um método "criarEstadio"
    // que recebe uma "comanda" (RequestDTO) e retorna
    // o "prato pronto" (ResponseDTO) do estádio que acabou de ser criado (com o ID).
    EstadioResponseDTO criarEstadio(EstadioRequestDTO estadioDTO);

    // Contrato 4: Deve existir um método "atualizarEstadio"
    // que recebe o "id" do estádio a ser atualizado e a "comanda" (RequestDTO)
    // com os novos dados. Retorna um Optional do "prato pronto" atualizado.
    Optional<EstadioResponseDTO> atualizarEstadio(Integer id, EstadioRequestDTO estadioDTO);

    // Contrato 5: Deve existir um método "deletarEstadio"
    // que recebe um "id" e retorna "true" se conseguiu deletar
    // ou "false" se não encontrou o estádio para deletar.
    boolean deletarEstadio(Integer id);
}