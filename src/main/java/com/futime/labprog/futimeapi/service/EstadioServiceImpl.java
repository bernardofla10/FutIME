package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.EstadioRequestDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.repository.EstadioRepository;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service // Anotação que define esta classe como um Bean de Serviço (lógica de negócio)
public class EstadioServiceImpl implements EstadioService { // implementar os métodos do contrato EstadioService.

    // @Autowired // Injeção de Dependência diretamente na declaração do campo
    private final EstadioRepository estadioRepository; // dependência do serviço: repositório.

    // Injeção de Dependência via Construtor (Boa Prática SOLID)
    public EstadioServiceImpl(EstadioRepository estadioRepository) {
        this.estadioRepository = estadioRepository;
    } // permite ver claramente todas as dependências da classe

    // Métodos de conversão privados (lógica interna do serviço)
    private EstadioResponseDTO toResponseDTO(Estadio estadio) {
        return new EstadioResponseDTO(estadio.getId(), estadio.getNome(), estadio.getCidade(), estadio.getPais());
    } // Converte o "ingrediente cru" (Entidade Estadio) no "prato pronto" (ResponseDTO).

    private Estadio toEntity(EstadioRequestDTO dto) {
        return new Estadio(dto.nome(), dto.cidade(), dto.pais());
    } // // Converte a "comanda" (RequestDTO) no "ingrediente cru" (Entidade Estadio).

    @Override // sobrescrevendo um método da interface
    @Transactional(readOnly = true) // gerenciar uma transação com o BD para esse método que apenas lê, não escreve.
    public List<EstadioResponseDTO> listarEstadios() {
        // 1. Pede ao Almoxarife (Repository) todos os ingredientes (Entidades).
        return estadioRepository.findAll().stream() // 2. Para cada ingrediente... stream() processa um item da lista cada vez (esteira de produção).
                .map(this::toResponseDTO) // // 3. ...traduz para um "prato pronto" (ResponseDTO)... map() aplica uma transformação a cada item da lista (estação).
                .collect(Collectors.toList()); // // 4. ...e coleta em uma lista. // um item, ao sair da estação (map), retorna a esteira (stream) e chega a uma List nova.
    } // this::toResponseDTO é um atalho para estadio -> this.toResponseDTO(estadio).
    // this se refere à classe EstadioServiceImpl e toResponseDTO transforma um Estadio em um EstadioResponseDTO: ingrediente cru -> prato pronto.

    @Override
    @Transactional(readOnly = true)
    public Optional<EstadioResponseDTO> buscarEstadioPorId(Integer id) {
        // 1. Pede ao Almoxarife (Repository) o ingrediente com esse "id".
        return estadioRepository.findById(id)
                // 2. Se o ingrediente for encontrado...
                .map(this::toResponseDTO); // 3. ...traduz para "prato pronto" (ResponseDTO).
                // Se não for encontrado, o "Optional" continua vazio.
    }

    @Override
    @Transactional // gerenciar uma transação com o BD para esse método que escreve no BD.
    public EstadioResponseDTO criarEstadio(EstadioRequestDTO estadioDTO) {
        // 1. Traduz a "comanda" (RequestDTO) para "ingrediente cru" (Entidade).
        Estadio novoEstadio = toEntity(estadioDTO);
        // 2. Manda o Almoxarife (Repository) salvar o ingrediente.
        Estadio estadioSalvo = estadioRepository.save(novoEstadio);
        // 3. Traduz o ingrediente salvo (agora com ID) para "prato pronto" (ResponseDTO).
        return toResponseDTO(estadioSalvo);
    }

    @Override
    @Transactional
    public Optional<EstadioResponseDTO> atualizarEstadio(Integer id, EstadioRequestDTO estadioDTO) {
        // 1. Pede ao Almoxarife (Repository) o ingrediente com esse "id".
        return estadioRepository.findById(id)
                // 2. Se o ingrediente for encontrado (chamamos de "estadioExistente")...
                .map(estadioExistente -> {
                    // 3. Atualiza os dados do ingrediente existente com os dados da "comanda" (RequestDTO).
                    estadioExistente.setNome(estadioDTO.nome());
                    estadioExistente.setCidade(estadioDTO.cidade());
                    estadioExistente.setPais(estadioDTO.pais());
                    
                    // 4. Manda o Almoxarife (Repository) salvar o ingrediente atualizado.
                    Estadio estadioAtualizado = estadioRepository.save(estadioExistente);
                    // 5. Traduz o ingrediente atualizado para "prato pronto" (ResponseDTO).
                    return toResponseDTO(estadioAtualizado);
                }); // Se o findById não encontrar nada, o .map() é pulado e um Optional vazio é retornado.
    }

    @Override
    @Transactional
    public boolean deletarEstadio(Integer id) {
        // 1. Pergunta ao Almoxarife (Repository) se um item com esse "id" existe.
        if (estadioRepository.existsById(id)) {
            // 2. Se existir, manda o Almoxarife (Repository) deletar.
            estadioRepository.deleteById(id);
            // 3. Retorna "true" para o Controller saber que deu certo.
            return true;
        }
        // 4. Se não existia, retorna "false".
        return false;
    }
}