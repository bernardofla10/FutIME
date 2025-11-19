package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoRequestDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Competicao;
import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.CompeticaoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompeticaoServiceImpl implements CompeticaoService {

    private final CompeticaoRepository competicaoRepository;
    private final ClubeRepository clubeRepository;

    public CompeticaoServiceImpl(CompeticaoRepository competicaoRepository, ClubeRepository clubeRepository) {
        this.competicaoRepository = competicaoRepository;
        this.clubeRepository = clubeRepository;
    }

    private CompeticaoResponseDTO toResponseDTO(Competicao competicao) {
        List<ClubeResponseDTO> clubesDTO = null;
        if (competicao.getClubes() != null && !competicao.getClubes().isEmpty()) {
            clubesDTO = competicao.getClubes().stream()
                    .map(clube -> {
                        EstadioResponseDTO estadioDTO = null;
                        if (clube.getEstadio() != null) {
                            Estadio estadio = clube.getEstadio();
                            estadioDTO = new EstadioResponseDTO(
                                    estadio.getId(),
                                    estadio.getNome(),
                                    estadio.getCidade(),
                                    estadio.getPais());
                        }

                        return new ClubeResponseDTO(
                                clube.getId(),
                                clube.getNome(),
                                clube.getSigla(),
                                clube.getCidade(),
                                clube.getPais(),
                                estadioDTO);
                    })
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
                competicao.getUpdatedAt());
    }

    private Competicao toEntity(CompeticaoRequestDTO dto) {
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
        competicao.setClubes(clubes);
        return competicao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompeticaoResponseDTO> listarCompeticoes() {
        return competicaoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompeticaoResponseDTO buscarCompeticaoPorId(Integer id) {
        return competicaoRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Competição não encontrada com ID: " + id));
    }

    @Override
    @Transactional
    public CompeticaoResponseDTO criarCompeticao(CompeticaoRequestDTO competicaoDTO) {
        Competicao novaCompeticao = toEntity(competicaoDTO);
        Competicao competicaoSalva = competicaoRepository.save(novaCompeticao);
        return toResponseDTO(competicaoSalva);
    }

    @Override
    @Transactional
    public CompeticaoResponseDTO atualizarCompeticao(Integer id, CompeticaoRequestDTO competicaoDTO) {
        Competicao competicaoExistente = competicaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Competição não encontrada com ID: " + id));

        List<Clube> novosClubes = competicaoDTO.clubeIds().stream()
                .map(clubeId -> clubeRepository.findById(clubeId)
                        .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + clubeId + " não encontrado.")))
                .collect(Collectors.toList());

        competicaoExistente.setNome(competicaoDTO.nome());
        competicaoExistente.setPais(competicaoDTO.pais());
        competicaoExistente.setContinente(competicaoDTO.continente());
        competicaoExistente.setTipoCompeticao(competicaoDTO.tipoCompeticao());
        competicaoExistente.setTemporada(competicaoDTO.temporada());
        competicaoExistente.setClubes(novosClubes);

        Competicao competicaoAtualizada = competicaoRepository.save(competicaoExistente);
        return toResponseDTO(competicaoAtualizada);
    }

    @Override
    @Transactional
    public void deletarCompeticao(Integer id) {
        if (!competicaoRepository.existsById(id)) {
            throw new EntityNotFoundException("Competição não encontrada com ID: " + id);
        }
        competicaoRepository.deleteById(id);
    }
}