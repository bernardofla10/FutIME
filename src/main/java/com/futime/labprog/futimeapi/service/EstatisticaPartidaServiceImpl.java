package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.EstatisticaPartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticaPartidaResponseDTO;
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.model.JogadorEstatisticaPartida;
import com.futime.labprog.futimeapi.model.Partida;
import com.futime.labprog.futimeapi.repository.JogadorEstatisticaPartidaRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
import com.futime.labprog.futimeapi.repository.PartidaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstatisticaPartidaServiceImpl implements EstatisticaPartidaService {

    private final JogadorEstatisticaPartidaRepository estatisticaRepository;
    private final JogadorRepository jogadorRepository;
    private final PartidaRepository partidaRepository;

    public EstatisticaPartidaServiceImpl(JogadorEstatisticaPartidaRepository estatisticaRepository, JogadorRepository jogadorRepository, PartidaRepository partidaRepository) {
        this.estatisticaRepository = estatisticaRepository;
        this.jogadorRepository = jogadorRepository;
        this.partidaRepository = partidaRepository;
    }

    private EstatisticaPartidaResponseDTO toResponseDTO(JogadorEstatisticaPartida e) {
        return new EstatisticaPartidaResponseDTO(
                e.getId(),
                e.getJogador().getId(),
                e.getJogador().getApelido(),
                e.getPartida().getId(),
                e.getMinutosJogados(),
                e.isCartaoAmarelo(),
                e.isCartaoVermelho(),
                e.isTitular(),
                e.getGols(),
                e.getAssistencias(),
                e.getDefesa(),
                e.getFinalizacoes(),
                e.getChutesAGol(),
                e.getDesarmes(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private void apply(JogadorEstatisticaPartida target, EstatisticaPartidaRequestDTO dto) {
        target.setMinutosJogados(dto.minutosJogados());
        target.setCartaoAmarelo(dto.cartaoAmarelo());
        target.setCartaoVermelho(dto.cartaoVermelho());
        target.setTitular(dto.titular());
        target.setGols(dto.gols());
        target.setAssistencias(dto.assistencias());
        target.setDefesa(dto.defesa());
        target.setFinalizacoes(dto.finalizacoes());
        target.setChutesAGol(dto.chutesAGol());
        target.setDesarmes(dto.desarmes());
    }

    @Override
    @Transactional
    public EstatisticaPartidaResponseDTO salvar(Integer jogadorId, Integer partidaId, EstatisticaPartidaRequestDTO dto) {
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new EntityNotFoundException("Jogador ID \" + jogadorId + \" não encontrado"));
        Partida partida = partidaRepository.findById(partidaId)
                .orElseThrow(() -> new EntityNotFoundException("Partida ID \" + partidaId + \" não encontrada"));

        JogadorEstatisticaPartida entity = estatisticaRepository.findByJogador_IdAndPartida_Id(jogadorId, partidaId)
                .orElseGet(() -> {
                    JogadorEstatisticaPartida novo = new JogadorEstatisticaPartida();
                    novo.setJogador(jogador);
                    novo.setPartida(partida);
                    return novo;
                });

        apply(entity, dto);
        JogadorEstatisticaPartida salvo = estatisticaRepository.save(entity);
        return toResponseDTO(salvo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstatisticaPartidaResponseDTO> listarPorJogador(Integer jogadorId) {
        return estatisticaRepository.findByJogador_Id(jogadorId)
                .stream().map(this::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EstatisticaPartidaResponseDTO> buscarPorId(Integer id) {
        return estatisticaRepository.findById(id).map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public boolean deletar(Integer id) {
        if (!estatisticaRepository.existsById(id)) return false;
        estatisticaRepository.deleteById(id);
        return true;
    }
}


