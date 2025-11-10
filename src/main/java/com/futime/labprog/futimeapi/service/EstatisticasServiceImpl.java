package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.EstatisticasRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticasResponseDTO;
import com.futime.labprog.futimeapi.model.Competicao;
import com.futime.labprog.futimeapi.model.EstatisticasJogadorCompeticao;
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.repository.CompeticaoRepository;
import com.futime.labprog.futimeapi.repository.EstatisticasJogadorCompeticaoRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EstatisticasServiceImpl implements EstatisticasService {

    private final EstatisticasJogadorCompeticaoRepository estatisticasRepository;
    private final JogadorRepository jogadorRepository;
    private final CompeticaoRepository competicaoRepository;

    // Injeção de todas as dependências necessárias via construtor
    public EstatisticasServiceImpl(EstatisticasJogadorCompeticaoRepository estatisticasRepository,
                                   JogadorRepository jogadorRepository,
                                   CompeticaoRepository competicaoRepository) {
        this.estatisticasRepository = estatisticasRepository;
        this.jogadorRepository = jogadorRepository;
        this.competicaoRepository = competicaoRepository;
    }

    // --- MÉTODO DE TRADUÇÃO (PRIVADO) ---

    /**
     * ATUALIZADO: Converte Entidade Estatisticas... -> EstatisticasResponseDTO
     */
    private EstatisticasResponseDTO toResponseDTO(EstatisticasJogadorCompeticao stat) {
        return new EstatisticasResponseDTO(
                stat.getId(),
                stat.getGols(),
                stat.getAssistencias(),
                stat.getJogosDisputados(),
                stat.getJogador().getId(),
                stat.getJogador().getApelido(),
                stat.getCompeticao().getId(),
                stat.getCompeticao().getNome(), //
                stat.getCompeticao().getTemporada(), //
                stat.getCompeticao().getTipoCompeticao(), // ADICIONADO
                stat.getCompeticao().getCreatedAt() // ADICIONADO
        );
    }

    // --- IMPLEMENTAÇÃO DO CONTRATO (LÓGICA DE NEGÓCIO) ---

    @Override
    @Transactional
    public EstatisticasResponseDTO salvarEstatisticas(Integer jogadorId, Integer competicaoId, EstatisticasRequestDTO requestDTO) {
        
        // 1. Verificar se o registro já existe usando nosso novo método de repositório
        Optional<EstatisticasJogadorCompeticao> statExistenteOpt = estatisticasRepository.findByJogadorIdAndCompeticaoId(jogadorId, competicaoId);

        EstatisticasJogadorCompeticao estatisticaParaSalvar;

        if (statExistenteOpt.isPresent()) {
            // --- LÓGICA DE UPDATE ---
            estatisticaParaSalvar = statExistenteOpt.get();
            // Atualiza (substitui) os valores do registro existente
            estatisticaParaSalvar.setGols(requestDTO.gols());
            estatisticaParaSalvar.setAssistencias(requestDTO.assistencias());
            estatisticaParaSalvar.setJogosDisputados(requestDTO.jogosDisputados());

        } else {
            // --- LÓGICA DE CREATE ---
            // Se não existe, busca as entidades Jogador e Competicao para associar
            // Falha Rápido (Fail Fast) se o jogador ou competição não forem encontrados
            //
            Jogador jogador = jogadorRepository.findById(jogadorId)
                    .orElseThrow(() -> new EntityNotFoundException("Jogador com ID " + jogadorId + " não encontrado."));
            
            Competicao competicao = competicaoRepository.findById(competicaoId)
                    .orElseThrow(() -> new EntityNotFoundException("Competição com ID " + competicaoId + " não encontrada."));

            // Cria a nova entidade de estatística
            estatisticaParaSalvar = new EstatisticasJogadorCompeticao();
            estatisticaParaSalvar.setJogador(jogador);
            estatisticaParaSalvar.setCompeticao(competicao);
            estatisticaParaSalvar.setGols(requestDTO.gols());
            estatisticaParaSalvar.setAssistencias(requestDTO.assistencias());
            estatisticaParaSalvar.setJogosDisputados(requestDTO.jogosDisputados());
        }

        // 2. Salva (seja o objeto novo ou o atualizado)
        EstatisticasJogadorCompeticao estatisticaSalva = estatisticasRepository.save(estatisticaParaSalvar);

        // 3. Retorna o DTO
        return toResponseDTO(estatisticaSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstatisticasResponseDTO> listarEstatisticasPorJogador(Integer jogadorId) {
        // Usa nosso novo método de repositório
        return estatisticasRepository.findByJogadorId(jogadorId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EstatisticasResponseDTO> buscarEstatisticaPorId(Integer estatisticaId) {
        return estatisticasRepository.findById(estatisticaId)
                .map(this::toResponseDTO);
    }

    @Override
    @Transactional
    public boolean deletarEstatistica(Integer estatisticaId) {
        if (estatisticasRepository.existsById(estatisticaId)) {
            estatisticasRepository.deleteById(estatisticaId);
            return true;
        }
        return false;
    }
}