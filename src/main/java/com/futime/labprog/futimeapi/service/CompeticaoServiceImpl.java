package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoRequestDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Competicao;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.CompeticaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompeticaoServiceImpl implements CompeticaoService {

    // O "Almoxarife" de Competições
    private final CompeticaoRepository competicaoRepository;
    // O "Almoxarife" de Clubes, necessário para a associação
    private final ClubeRepository clubeRepository;

    // Injeção de *ambas* as dependências via construtor (Padrão FutIME / SOLID-D)
    public CompeticaoServiceImpl(CompeticaoRepository competicaoRepository, ClubeRepository clubeRepository) {
        this.competicaoRepository = competicaoRepository;
        this.clubeRepository = clubeRepository;
    }

    // --- MÉTODOS DE TRADUÇÃO (PRIVADOS) ---

    // Converte o "ingrediente cru" (Competicao) no "prato pronto" (CompeticaoResponseDTO)
    private CompeticaoResponseDTO toResponseDTO(Competicao competicao) {
        // Lógica de tradução aninhada:
        // Pega os ingredientes "Clube" de dentro da "Competicao" e os transforma
        // nos "pratos prontos" ClubeResponseDTO.
        List<ClubeResponseDTO> clubesDTO = null;
        if (competicao.getClubes() != null && !competicao.getClubes().isEmpty()) {
            clubesDTO = competicao.getClubes().stream()
                    .map(clube -> new ClubeResponseDTO(
                            clube.getId(),
                            clube.getNome(),
                            clube.getSigla(),
                            clube.getCidade(),
                            clube.getPais(),
                            null // Não incluímos o estádio para evitar recursão infinita
                    ))
                    .collect(Collectors.toList());
        }

        return new CompeticaoResponseDTO(
                competicao.getId(),
                competicao.getNome(),
                competicao.getPais(),
                competicao.getContinente(),
                competicao.getTipoCompeticao(),
                competicao.getTemporada(),
                clubesDTO,
                competicao.getCreatedAt(),
                competicao.getUpdatedAt()
        );
    }

    // Este método agora tem LÓGICA DE NEGÓCIO.
    // Ele não apenas converte, mas também VALIDA a "comanda" (RequestDTO).
    private Competicao toEntity(CompeticaoRequestDTO dto) {
        // Lógica de Negócio: Buscar os clubes.
        // O que acontece se algum clubeId não existir?
        // O .orElseThrow() garante que a operação falhe AGORA (Fail Fast)
        List<Clube> clubes = dto.clubeIds().stream()
                .map(clubeId -> clubeRepository.findById(clubeId)
                        .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + clubeId + " não encontrado.")))
                .collect(Collectors.toList());

        Competicao competicao = new Competicao();
        competicao.setNome(dto.nome());
        competicao.setPais(dto.pais());
        competicao.setContinente(dto.continente());
        competicao.setTipoCompeticao(dto.tipoCompeticao());
        competicao.setTemporada(dto.temporada());
        competicao.setClubes(clubes); // Associa a LISTA de ENTIDADES Clube
        return competicao;
    }

    // --- MÉTODOS PÚBLICOS (O CONTRATO) ---

    @Override
    @Transactional(readOnly = true)
    public List<CompeticaoResponseDTO> listarCompeticoes() {
        return competicaoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompeticaoResponseDTO> buscarCompeticaoPorId(Integer id) {
        return competicaoRepository.findById(id).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public CompeticaoResponseDTO criarCompeticao(CompeticaoRequestDTO competicaoDTO) {
        // 1. Converte a "comanda" (DTO) em "ingrediente" (Entidade)
        //    (O método toEntity() já faz a busca e validação dos Clubes)
        Competicao novaCompeticao = toEntity(competicaoDTO);
        
        // 2. Salva o novo ingrediente
        Competicao competicaoSalva = competicaoRepository.save(novaCompeticao);
        
        // 3. Converte o ingrediente salvo no "prato pronto"
        return toResponseDTO(competicaoSalva);
    }

    @Override
    @Transactional
    public Optional<CompeticaoResponseDTO> atualizarCompeticao(Integer id, CompeticaoRequestDTO competicaoDTO) {
        return competicaoRepository.findById(id)
                .map(competicaoExistente -> {
                    // Busca as *novas* entidades Clube
                    List<Clube> novosClubes = competicaoDTO.clubeIds().stream()
                            .map(clubeId -> clubeRepository.findById(clubeId)
                                    .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + clubeId + " não encontrado.")))
                            .collect(Collectors.toList());

                    // Atualiza os campos da competição existente
                    competicaoExistente.setNome(competicaoDTO.nome());
                    competicaoExistente.setPais(competicaoDTO.pais());
                    competicaoExistente.setContinente(competicaoDTO.continente());
                    competicaoExistente.setTipoCompeticao(competicaoDTO.tipoCompeticao());
                    competicaoExistente.setTemporada(competicaoDTO.temporada());
                    competicaoExistente.setClubes(novosClubes); // Associa os novos clubes

                    Competicao competicaoAtualizada = competicaoRepository.save(competicaoExistente);
                    return toResponseDTO(competicaoAtualizada);
                });
    }

    @Override
    @Transactional
    public boolean deletarCompeticao(Integer id) {
        if (competicaoRepository.existsById(id)) {
            competicaoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
