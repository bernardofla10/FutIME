package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.dto.PartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.PartidaResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.model.Partida;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.EstadioRepository;
import com.futime.labprog.futimeapi.repository.PartidaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartidaServiceImpl implements PartidaService {

    private final PartidaRepository partidaRepository;
    private final ClubeRepository clubeRepository;
    private final EstadioRepository estadioRepository;

    public PartidaServiceImpl(PartidaRepository partidaRepository, ClubeRepository clubeRepository, EstadioRepository estadioRepository) {
        this.partidaRepository = partidaRepository;
        this.clubeRepository = clubeRepository;
        this.estadioRepository = estadioRepository;
    }

    private EstadioResponseDTO toEstadioDTO(Estadio estadio) {
        return new EstadioResponseDTO(estadio.getId(), estadio.getNome(), estadio.getCidade(), estadio.getPais());
    }

    private ClubeResponseDTO toClubeDTO(Clube clube) {
        EstadioResponseDTO estadioDTO = null;
        if (clube.getEstadio() != null) {
            estadioDTO = toEstadioDTO(clube.getEstadio());
        }
        return new ClubeResponseDTO(clube.getId(), clube.getNome(), clube.getSigla(), clube.getCidade(), clube.getPais(), estadioDTO);
    }

    private PartidaResponseDTO toResponseDTO(Partida partida) {
        return new PartidaResponseDTO(
                partida.getId(),
                toClubeDTO(partida.getMandante()),
                toClubeDTO(partida.getVisitante()),
                toEstadioDTO(partida.getEstadio()),
                partida.getFase(),
                partida.getGolsMandante(),
                partida.getGolsVisitante(),
                partida.getDataHora(),
                partida.getCreatedAt(),
                partida.getUpdatedAt()
        );
    }

    private Partida toEntity(PartidaRequestDTO dto) {
        Clube mandante = clubeRepository.findById(dto.mandanteId())
                .orElseThrow(() -> new EntityNotFoundException("Clube mandante ID " + dto.mandanteId() + " não encontrado"));
        Clube visitante = clubeRepository.findById(dto.visitanteId())
                .orElseThrow(() -> new EntityNotFoundException("Clube visitante ID " + dto.visitanteId() + " não encontrado"));
        Estadio estadio = estadioRepository.findById(dto.estadioId())
                .orElseThrow(() -> new EntityNotFoundException("Estádio ID " + dto.estadioId() + " não encontrado"));

        Partida p = new Partida();
        p.setMandante(mandante);
        p.setVisitante(visitante);
        p.setEstadio(estadio);
        p.setFase(dto.fase());
        p.setGolsMandante(dto.golsMandante());
        p.setGolsVisitante(dto.golsVisitante());
        p.setDataHora(dto.dataHora());
        return p;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartidaResponseDTO> listarPartidas() {
        return partidaRepository.findAll().stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PartidaResponseDTO> buscarPorId(Integer id) {
        return partidaRepository.findById(id).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public PartidaResponseDTO criarPartida(PartidaRequestDTO dto) {
        Partida salva = partidaRepository.save(toEntity(dto));
        return toResponseDTO(salva);
    }

    @Override
    @Transactional
    public Optional<PartidaResponseDTO> atualizarPartida(Integer id, PartidaRequestDTO dto) {
        return partidaRepository.findById(id).map(existing -> {
            Clube mandante = clubeRepository.findById(dto.mandanteId())
                    .orElseThrow(() -> new EntityNotFoundException("Clube mandante ID " + dto.mandanteId() + " não encontrado"));
            Clube visitante = clubeRepository.findById(dto.visitanteId())
                    .orElseThrow(() -> new EntityNotFoundException("Clube visitante ID " + dto.visitanteId() + " não encontrado"));
            Estadio estadio = estadioRepository.findById(dto.estadioId())
                    .orElseThrow(() -> new EntityNotFoundException("Estádio ID " + dto.estadioId() + " não encontrado"));
            existing.setMandante(mandante);
            existing.setVisitante(visitante);
            existing.setEstadio(estadio);
            existing.setFase(dto.fase());
            existing.setGolsMandante(dto.golsMandante());
            existing.setGolsVisitante(dto.golsVisitante());
            existing.setDataHora(dto.dataHora());
            return toResponseDTO(partidaRepository.save(existing));
        });
    }

    @Override
    @Transactional
    public boolean deletarPartida(Integer id) {
        if (!partidaRepository.existsById(id)) return false;
        partidaRepository.deleteById(id);
        return true;
    }
}


