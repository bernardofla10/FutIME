package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.*; // Importa todos os DTOs
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.EstatisticasJogadorCompeticao; // Importa a nova entidade
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
// O Repositório de Estatísticas NÃO é necessário aqui (ainda),
// pois as estatísticas são carregadas via a entidade Jogador (Lazy Loading).
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map; // Para o agrupamento
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class JogadorServiceImpl implements JogadorService {

    private final JogadorRepository jogadorRepository;
    private final ClubeRepository clubeRepository;

    public JogadorServiceImpl(JogadorRepository jogadorRepository, ClubeRepository clubeRepository) {
        this.jogadorRepository = jogadorRepository;
        this.clubeRepository = clubeRepository;
    }

    // --- MÉTODOS DE TRADUÇÃO (PRIVADOS - ATUALIZADOS) ---

    /**
     * ATUALIZADO: Converte JogadorRequestDTO -> Entidade Jogador
     * (Agora inclui valorDeMercado)
     */
    private Jogador toEntity(JogadorRequestDTO dto) {
        // Lógica de Negócio: Buscar o Clube
        Clube clube = clubeRepository.findById(dto.clubeId())
                .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + dto.clubeId() + " não encontrado."));

        Jogador jogador = new Jogador();
        jogador.setNomeCompleto(dto.nomeCompleto());
        jogador.setApelido(dto.apelido());
        jogador.setDataNascimento(dto.dataNascimento());
        jogador.setPosicao(dto.posicao());
        jogador.setClube(clube);
        jogador.setValorDeMercado(dto.valorDeMercado()); // Adicionado
        // Note: As estatísticas não são adicionadas aqui. Elas são gerenciadas separadamente.
        return jogador;
    }

    /**
     * ATUALIZADO: Converte Entidade Jogador -> JogadorResponseDTO
     * (Corrigido para usar List e preencher DTOs atualizados)
     */
    private JogadorResponseDTO toResponseDTO(Jogador jogador) {
        
        // --- 1. Traduzir o Clube (lógica que já tínhamos) ---
        ClubeResponseDTO clubeDTO = null;
        if (jogador.getClube() != null) {
            // ... (A lógica de tradução do Clube para ClubeResponseDTO permanece a mesma) ...
            // [código omitido por brevidade]
        }

        // --- 2. Processar Estatísticas (LÓGICA ATUALIZADA) ---
        List<EstatisticasJogadorCompeticao> estatisticas = jogador.getEstatisticas(); // Agora é uma List

        // 2a. Calcular Totais (Gols e Assistências)
        int golsTotais = estatisticas.stream()
                .mapToInt(EstatisticasJogadorCompeticao::getGols)
                .sum();
        int assistenciasTotais = estatisticas.stream()
                .mapToInt(EstatisticasJogadorCompeticao::getAssistencias)
                .sum();

        // 2b. Mapear Estatísticas por Competição (Tradução ATUALIZADA)
        List<EstatisticaCompeticaoDTO> estatisticasPorCompeticao = estatisticas.stream()
                .map(stat -> new EstatisticaCompeticaoDTO(
                        stat.getCompeticao().getNome(),
                        stat.getCompeticao().getTemporada(),
                        stat.getCompeticao().getTipoCompeticao(), // CAMPO ADICIONADO
                        stat.getGols(),
                        stat.getAssistencias()
                ))
                .collect(Collectors.toList()); // ATUALIZADO para toList()

        // 2c. Calcular Estatísticas por Temporada (Agrupamento)
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
                    
                    return new EstatisticaTemporadaDTO(temporada, golsDaTemporada, assistenciasDaTemporada);
                })
                .collect(Collectors.toList()); // ATUALIZADO para toList()


        // --- 3. Montar o Response DTO Final ---
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
                estatisticasPorTemporada, // Agora é List
                estatisticasPorCompeticao // Agora é List
        );
    }

    // --- MÉTODOS PÚBLICOS (O CONTRATO - ATUALIZADOS ONDE NECESSÁRIO) ---

    @Override
    @Transactional(readOnly = true)
    public List<JogadorResponseDTO> listarJogadores() {
        return jogadorRepository.findAll().stream()
                .map(this::toResponseDTO) // Agora chama o toResponseDTO complexo
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<JogadorResponseDTO> buscarJogadorPorId(Integer id) {
        return jogadorRepository.findById(id)
                .map(this::toResponseDTO); // Agora chama o toResponseDTO complexo
    }

    @Override
    @Transactional
    public JogadorResponseDTO criarJogador(JogadorRequestDTO jogadorDTO) {
        // Usa o toEntity atualizado (que inclui valorDeMercado)
        Jogador novoJogador = toEntity(jogadorDTO);
        Jogador jogadorSalvo = jogadorRepository.save(novoJogador);
        // Chama o toResponseDTO complexo, que (para um novo jogador)
        // retornará as estatísticas como 0 e listas vazias, o que está correto.
        return toResponseDTO(jogadorSalvo);
    }

    @Override
    @Transactional
    public Optional<JogadorResponseDTO> atualizarJogador(Integer id, JogadorRequestDTO jogadorDTO) {
        return jogadorRepository.findById(id)
                .map(jogadorExistente -> {
                    // Busca e valida o Clube (lógica do toEntity, mas para atualização)
                    Clube novoClube = clubeRepository.findById(jogadorDTO.clubeId())
                            .orElseThrow(() -> new EntityNotFoundException("Clube com ID " + jogadorDTO.clubeId() + " não encontrado."));
                    
                    // Atualiza os campos simples
                    jogadorExistente.setNomeCompleto(jogadorDTO.nomeCompleto());
                    jogadorExistente.setApelido(jogadorDTO.apelido());
                    jogadorExistente.setDataNascimento(jogadorDTO.dataNascimento());
                    jogadorExistente.setPosicao(jogadorDTO.posicao());
                    jogadorExistente.setValorDeMercado(jogadorDTO.valorDeMercado()); // Adicionado
                    jogadorExistente.setClube(novoClube);

                    Jogador jogadorAtualizado = jogadorRepository.save(jogadorExistente);
                    // Retorna o DTO complexo com todas as estatísticas calculadas
                    return toResponseDTO(jogadorAtualizado);
                });
    }

    @Override
    @Transactional
    public boolean deletarJogador(Integer id) {
        // Nenhuma mudança na lógica de deleção.
        // Graças ao 'cascade = CascadeType.ALL' no Jogador.java,
        // ao deletar o jogador, o Hibernate automaticamente deletará
        // todas as suas estatísticas associadas.
        if (jogadorRepository.existsById(id)) {
            jogadorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
