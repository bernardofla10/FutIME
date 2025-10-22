package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeResponseDTO; // Necessário para DTO aninhado
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO; // Necessário para DTO aninhado
import com.futime.labprog.futimeapi.dto.JogadorRequestDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;
import com.futime.labprog.futimeapi.model.Clube; // Precisa da entidade Clube
import com.futime.labprog.futimeapi.model.Jogador; // Precisa da entidade Jogador
import com.futime.labprog.futimeapi.repository.ClubeRepository; // Dependência 1
import com.futime.labprog.futimeapi.repository.JogadorRepository; // Dependência 2
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JogadorServiceImpl implements JogadorService {

    private final JogadorRepository jogadorRepository;
    private final ClubeRepository clubeRepository; // Injetamos o repo do Clube

    // Injeção via construtor de ambas dependências
    public JogadorServiceImpl(JogadorRepository jogadorRepository, ClubeRepository clubeRepository) {
        this.jogadorRepository = jogadorRepository;
        this.clubeRepository = clubeRepository;
    }

    // --- MÉTODOS DE TRADUÇÃO (PRIVADOS) ---

    // Converte Entidade Jogador -> JogadorResponseDTO (com Clube aninhado)
    private JogadorResponseDTO toResponseDTO(Jogador jogador) {
        ClubeResponseDTO clubeDTO = null;
        if (jogador.getClube() != null) {
            Clube clube = jogador.getClube();
            // Precisamos traduzir o Estadio DENTRO do Clube também
            EstadioResponseDTO estadioDTO = null;
            if (clube.getEstadio() != null) {
                estadioDTO = new EstadioResponseDTO(
                        clube.getEstadio().getId(),
                        clube.getEstadio().getNome(),
                        clube.getEstadio().getCidade(),
                        clube.getEstadio().getPais() // Usando 'pais' conforme atualização
                );
            }
            clubeDTO = new ClubeResponseDTO(
                    clube.getId(),
                    clube.getNome(),
                    clube.getSigla(),
                    clube.getCidade(),
                    clube.getPais(), // Usando 'pais'
                    estadioDTO
            );
        }

        return new JogadorResponseDTO(
                jogador.getId(),
                jogador.getNomeCompleto(),
                jogador.getApelido(),
                jogador.getDataNascimento(),
                jogador.getPosicao(),
                clubeDTO // Inclui o DTO aninhado do Clube (que por sua vez tem o do Estadio)
        );
    }

    // Converte JogadorRequestDTO -> Entidade Jogador (buscando e validando o Clube)
    private Jogador toEntity(JogadorRequestDTO dto) {
        // Lógica de Negócio: Buscar o Clube
        Clube clube = clubeRepository.findById(dto.clubeId())
                .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + dto.clubeId() + " não encontrado."));

        Jogador jogador = new Jogador();
        jogador.setNomeCompleto(dto.nomeCompleto());
        jogador.setApelido(dto.apelido());
        jogador.setDataNascimento(dto.dataNascimento());
        jogador.setPosicao(dto.posicao());
        jogador.setClube(clube); // Associa a ENTIDADE Clube
        return jogador;
    }

    // --- MÉTODOS PÚBLICOS (O CONTRATO) ---

    @Override
    @Transactional(readOnly = true)
    public List<JogadorResponseDTO> listarJogadores() {
        return jogadorRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JogadorResponseDTO> buscarJogadorPorId(Integer id) {
        return jogadorRepository.findById(id).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public JogadorResponseDTO criarJogador(JogadorRequestDTO jogadorDTO) {
        // O método toEntity já busca e valida o Clube
        Jogador novoJogador = toEntity(jogadorDTO);
        Jogador jogadorSalvo = jogadorRepository.save(novoJogador);
        return toResponseDTO(jogadorSalvo);
    }

    @Override
    @Transactional
    public Optional<JogadorResponseDTO> atualizarJogador(Integer id, JogadorRequestDTO jogadorDTO) {
        return jogadorRepository.findById(id)
                .map(jogadorExistente -> {
                    // Busca e valida a *nova* entidade Clube
                    Clube novoClube = clubeRepository.findById(jogadorDTO.clubeId())
                            .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + jogadorDTO.clubeId() + " não encontrado."));

                    // Atualiza os campos do jogador existente
                    jogadorExistente.setNomeCompleto(jogadorDTO.nomeCompleto());
                    jogadorExistente.setApelido(jogadorDTO.apelido());
                    jogadorExistente.setDataNascimento(jogadorDTO.dataNascimento());
                    jogadorExistente.setPosicao(jogadorDTO.posicao());
                    jogadorExistente.setClube(novoClube); // Associa o novo clube

                    Jogador jogadorAtualizado = jogadorRepository.save(jogadorExistente);
                    return toResponseDTO(jogadorAtualizado);
                });
    }

    @Override
    @Transactional
    public boolean deletarJogador(Integer id) {
        if (jogadorRepository.existsById(id)) {
            jogadorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
