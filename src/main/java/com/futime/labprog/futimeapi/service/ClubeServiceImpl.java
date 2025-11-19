package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeRequestDTO;
import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.CompeticaoRepository;
import com.futime.labprog.futimeapi.repository.EstadioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClubeServiceImpl implements ClubeService {

    private final ClubeRepository clubeRepository;
    private final EstadioRepository estadioRepository;
    private final CompeticaoRepository competicaoRepository;

    public ClubeServiceImpl(ClubeRepository clubeRepository, EstadioRepository estadioRepository,
            CompeticaoRepository competicaoRepository) {
        this.clubeRepository = clubeRepository;
        this.estadioRepository = estadioRepository;
        this.competicaoRepository = competicaoRepository;
    }

    private ClubeResponseDTO toResponseDTO(Clube clube) {
        EstadioResponseDTO estadioDTO = null;
        if (clube.getEstadio() != null) {
            Estadio estadio = clube.getEstadio();
            estadioDTO = new EstadioResponseDTO(estadio.getId(), estadio.getNome(), estadio.getCidade(),
                    estadio.getPais());
        }

        return new ClubeResponseDTO(
                clube.getId(),
                clube.getNome(),
                clube.getSigla(),
                clube.getCidade(),
                clube.getPais(),
                estadioDTO);
    }

    private Clube toEntity(ClubeRequestDTO dto) {
        Estadio estadio = null;
        if (dto.estadioId() != null) {
            estadio = estadioRepository.findById(dto.estadioId())
                    .orElseThrow(
                            () -> new EntityNotFoundException(
                                    "Estádio com ID " + dto.estadioId() + " não encontrado."));
        }

        Clube clube = new Clube();
        clube.setNome(dto.nome());
        clube.setSigla(dto.sigla());
        clube.setCidade(dto.cidade());
        clube.setPais(dto.pais());
        clube.setEstadio(estadio);
        return clube;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubeResponseDTO> listarClubes() {
        return clubeRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClubeResponseDTO buscarClubePorId(Integer id) {
        return clubeRepository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new EntityNotFoundException("Clube não encontrado com ID: " + id));
    }

    @Override
    @Transactional
    public ClubeResponseDTO criarClube(ClubeRequestDTO clubeDTO) {
        Clube novoClube = toEntity(clubeDTO);
        Clube clubeSalvo = clubeRepository.save(novoClube);
        return toResponseDTO(clubeSalvo);
    }

    @Override
    @Transactional
    public ClubeResponseDTO atualizarClube(Integer id, ClubeRequestDTO clubeDTO) {
        Clube clubeExistente = clubeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Clube não encontrado com ID: " + id));

        Estadio novoEstadio = null;
        if (clubeDTO.estadioId() != null) {
            novoEstadio = estadioRepository.findById(clubeDTO.estadioId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Estádio com ID " + clubeDTO.estadioId() + " não encontrado."));
        }

        clubeExistente.setNome(clubeDTO.nome());
        clubeExistente.setSigla(clubeDTO.sigla());
        clubeExistente.setCidade(clubeDTO.cidade());
        clubeExistente.setPais(clubeDTO.pais());
        clubeExistente.setEstadio(novoEstadio);

        Clube clubeAtualizado = clubeRepository.save(clubeExistente);
        return toResponseDTO(clubeAtualizado);
    }

    @Override
    @Transactional
    public void deletarClube(Integer id) {
        if (!clubeRepository.existsById(id)) {
            throw new EntityNotFoundException("Clube não encontrado com ID: " + id);
        }

        competicaoRepository.findByClubes_Id(id).forEach(competicao -> {
            if (competicao.getClubes() != null) {
                competicao.getClubes().removeIf(c -> c.getId() != null && c.getId().equals(id));
            }
            competicaoRepository.save(competicao);
        });

        clubeRepository.deleteById(id);
    }
}
