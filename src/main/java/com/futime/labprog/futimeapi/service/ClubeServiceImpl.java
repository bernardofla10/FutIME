package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeRequestDTO;
import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.EstadioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException; // Usaremos para tratar erros
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClubeServiceImpl implements ClubeService {

    // O "Almoxarife" de Clubes
    private final ClubeRepository clubeRepository;
    // O "Almoxarife" de Estádios, necessário para a associação
    private final EstadioRepository estadioRepository;

    // Injeção de *ambas* as dependências via construtor (Padrão FutIME / SOLID-D)
    //
    public ClubeServiceImpl(ClubeRepository clubeRepository, EstadioRepository estadioRepository) {
        this.clubeRepository = clubeRepository;
        this.estadioRepository = estadioRepository;
    } // precisa do ClubeRepository (para salvar o clube) e do EstadioRepository (para encontrar o estádio que será associado).

    // --- MÉTODOS DE TRADUÇÃO (PRIVADOS) ---

    // Converte o "ingrediente cru" (Clube) no "prato pronto" (ClubeResponseDTO)
    private ClubeResponseDTO toResponseDTO(Clube clube) {
        // Lógica de tradução aninhada:
        // Pega o ingrediente "Estadio" de dentro do "Clube" e o transforma
        // no "prato pronto" EstadioResponseDTO.
        EstadioResponseDTO estadioDTO = null;
        if (clube.getEstadio() != null) {
            Estadio estadio = clube.getEstadio();
            estadioDTO = new EstadioResponseDTO(estadio.getId(), estadio.getNome(), estadio.getCidade(), estadio.getPais());
        }

        return new ClubeResponseDTO(
                clube.getId(),
                clube.getNome(),
                clube.getSigla(),
                clube.getCidade(),
                clube.getPais(),
                estadioDTO // Retorna o DTO aninhado
        );
    }

    // Este método agora tem LÓGICA DE NEGÓCIO.
    // Ele não apenas converte, mas também VALIDA a "comanda" (RequestDTO).
    private Clube toEntity(ClubeRequestDTO dto) {
        // Lógica de Negócio: Buscar o estádio.
        // O que acontece se o estadioId não existir?
        // O .orElseThrow() garante que a operação falhe AGORA (Fail Fast)
        //
        Estadio estadio = estadioRepository.findById(dto.estadioId())
                .orElseThrow(() -> new EntityNotFoundException("Estádio com ID " + dto.estadioId() + " não encontrado."));

        Clube clube = new Clube();
        clube.setNome(dto.nome());
        clube.setSigla(dto.sigla());
        clube.setCidade(dto.cidade());
        clube.setPais(dto.pais());
        clube.setEstadio(estadio); // Associa a ENTIDADE Estadio
        return clube;
    }

    // --- MÉTODOS PÚBLICOS (O CONTRATO) ---

    @Override
    @Transactional(readOnly = true)
    public List<ClubeResponseDTO> listarClubes() {
        return clubeRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ClubeResponseDTO> buscarClubePorId(Integer id) {
        return clubeRepository.findById(id).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public ClubeResponseDTO criarClube(ClubeRequestDTO clubeDTO) {
        // 1. Converte a "comanda" (DTO) em "ingrediente" (Entidade)
        //    (O método toEntity() já faz a busca e validação do Estádio)
        Clube novoClube = toEntity(clubeDTO);
        
        // 2. Salva o novo ingrediente
        Clube clubeSalvo = clubeRepository.save(novoClube);
        
        // 3. Converte o ingrediente salvo no "prato pronto"
        return toResponseDTO(clubeSalvo);
    }

    @Override
    @Transactional
    public Optional<ClubeResponseDTO> atualizarClube(Integer id, ClubeRequestDTO clubeDTO) {
        return clubeRepository.findById(id)
                .map(clubeExistente -> {
                    // Busca a *nova* entidade Estadio
                    Estadio novoEstadio = estadioRepository.findById(clubeDTO.estadioId())
                            .orElseThrow(() -> new EntityNotFoundException("Estádio com ID " + clubeDTO.estadioId() + " não encontrado."));

                    // Atualiza os campos do clube existente
                    clubeExistente.setNome(clubeDTO.nome());
                    clubeExistente.setSigla(clubeDTO.sigla());
                    clubeExistente.setCidade(clubeDTO.cidade());
                    clubeExistente.setPais(clubeDTO.pais());
                    clubeExistente.setEstadio(novoEstadio); // Associa o novo estádio

                    Clube clubeAtualizado = clubeRepository.save(clubeExistente);
                    return toResponseDTO(clubeAtualizado);
                });
    }

    @Override
    @Transactional
    public boolean deletarClube(Integer id) {
        if (clubeRepository.existsById(id)) {
            clubeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}