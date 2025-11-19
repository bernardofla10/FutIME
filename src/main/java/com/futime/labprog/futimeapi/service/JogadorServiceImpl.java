package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.*;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.EstatisticasJogadorCompeticao;
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JogadorServiceImpl implements JogadorService {

        private final JogadorRepository jogadorRepository;
        private final ClubeRepository clubeRepository;

        public JogadorServiceImpl(JogadorRepository jogadorRepository, ClubeRepository clubeRepository) {
                this.jogadorRepository = jogadorRepository;
                this.clubeRepository = clubeRepository;
        }

        private Jogador toEntity(JogadorRequestDTO dto) {
                Clube clube = clubeRepository.findById(dto.clubeId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Clube com ID " + dto.clubeId() + " não encontrado."));

                Jogador jogador = new Jogador();
                jogador.setNomeCompleto(dto.nomeCompleto());
                jogador.setApelido(dto.apelido());
                jogador.setDataNascimento(dto.dataNascimento());
                jogador.setPosicao(dto.posicao());
                jogador.setClube(clube);
                jogador.setValorDeMercado(dto.valorDeMercado());
                return jogador;
        }

        @Override
        public JogadorResponseDTO toResponseDTO(Jogador jogador) {
                ClubeResponseDTO clubeDTO = null;
                if (jogador.getClube() != null) {
                        Clube clube = jogador.getClube();
                        EstadioResponseDTO estadioDTO = null;
                        if (clube.getEstadio() != null) {
                                estadioDTO = new EstadioResponseDTO(
                                                clube.getEstadio().getId(),
                                                clube.getEstadio().getNome(),
                                                clube.getEstadio().getCidade(),
                                                clube.getEstadio().getPais());
                        }
                        clubeDTO = new ClubeResponseDTO(
                                        clube.getId(),
                                        clube.getNome(),
                                        clube.getSigla(),
                                        clube.getCidade(),
                                        clube.getPais(),
                                        estadioDTO);
                }

                List<EstatisticasJogadorCompeticao> estatisticas = jogador.getEstatisticas();

                int golsTotais = estatisticas.stream()
                                .mapToInt(EstatisticasJogadorCompeticao::getGols)
                                .sum();
                int assistenciasTotais = estatisticas.stream()
                                .mapToInt(EstatisticasJogadorCompeticao::getAssistencias)
                                .sum();

                List<EstatisticaCompeticaoDTO> estatisticasPorCompeticao = estatisticas.stream()
                                .map(stat -> new EstatisticaCompeticaoDTO(
                                                stat.getCompeticao().getNome(),
                                                stat.getCompeticao().getTemporada(),
                                                stat.getCompeticao().getTipoCompeticao(),
                                                stat.getGols(),
                                                stat.getAssistencias()))
                                .collect(Collectors.toList());

                Map<String, List<EstatisticasJogadorCompeticao>> statsAgrupadasPorTemporada = estatisticas.stream()
                                .collect(Collectors.groupingBy(stat -> stat.getCompeticao().getTemporada()));

                List<EstatisticaTemporadaDTO> estatisticasPorTemporada = statsAgrupadasPorTemporada.entrySet().stream()
                                .map(entry -> {
                                        String temporada = entry.getKey();
                                        List<EstatisticasJogadorCompeticao> statsDaTemporada = entry.getValue();

                                        int golsDaTemporada = statsDaTemporada.stream()
                                                        .mapToInt(EstatisticasJogadorCompeticao::getGols)
                                                        .sum();
                                        int assistenciasDaTemporada = statsDaTemporada.stream()
                                                        .mapToInt(EstatisticasJogadorCompeticao::getAssistencias)
                                                        .sum();

                                        return new EstatisticaTemporadaDTO(temporada, golsDaTemporada,
                                                        assistenciasDaTemporada);
                                })
                                .collect(Collectors.toList());

                return new JogadorResponseDTO(
                                jogador.getId(),
                                jogador.getNomeCompleto(),
                                jogador.getApelido(),
                                jogador.getDataNascimento(),
                                jogador.getPosicao(),
                                jogador.getValorDeMercado(),
                                clubeDTO,
                                golsTotais,
                                assistenciasTotais,
                                estatisticasPorTemporada,
                                estatisticasPorCompeticao);
        }

        @Override
        @Transactional(readOnly = true)
        public List<JogadorResponseDTO> listarJogadores() {
                return jogadorRepository.findAll().stream()
                                .map(this::toResponseDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public JogadorResponseDTO buscarJogadorPorId(Integer id) {
                return jogadorRepository.findById(id)
                                .map(this::toResponseDTO)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Jogador não encontrado com ID: " + id));
        }

        @Override
        @Transactional
        public JogadorResponseDTO criarJogador(JogadorRequestDTO jogadorDTO) {
                Jogador novoJogador = toEntity(jogadorDTO);
                Jogador jogadorSalvo = jogadorRepository.save(novoJogador);
                return toResponseDTO(jogadorSalvo);
        }

        @Override
        @Transactional
        public JogadorResponseDTO atualizarJogador(Integer id, JogadorRequestDTO jogadorDTO) {
                Jogador jogadorExistente = jogadorRepository.findById(id)
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Jogador não encontrado com ID: " + id));

                Clube novoClube = clubeRepository.findById(jogadorDTO.clubeId())
                                .orElseThrow(() -> new EntityNotFoundException("Clube com ID "
                                                + jogadorDTO.clubeId() + " não encontrado."));

                jogadorExistente.setNomeCompleto(jogadorDTO.nomeCompleto());
                jogadorExistente.setApelido(jogadorDTO.apelido());
                jogadorExistente.setDataNascimento(jogadorDTO.dataNascimento());
                jogadorExistente.setPosicao(jogadorDTO.posicao());
                jogadorExistente.setValorDeMercado(jogadorDTO.valorDeMercado());
                jogadorExistente.setClube(novoClube);

                Jogador jogadorAtualizado = jogadorRepository.save(jogadorExistente);
                return toResponseDTO(jogadorAtualizado);
        }

        @Override
        @Transactional
        public void deletarJogador(Integer id) {
                if (!jogadorRepository.existsById(id)) {
                        throw new EntityNotFoundException("Jogador não encontrado com ID: " + id);
                }
                jogadorRepository.deleteById(id);
        }
}
