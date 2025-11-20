package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.EstatisticaTemporadaDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Competicao;
import com.futime.labprog.futimeapi.model.EstatisticasJogadorCompeticao;
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.model.TipoCompeticao;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JogadorServiceTest {

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private ClubeRepository clubeRepository;

    @InjectMocks
    private JogadorServiceImpl jogadorService;

    @Test
    @DisplayName("Deve calcular totais de gols e assistências e agrupar por temporada corretamente")
    void deveCalcularTotaisEAgruparPorTemporadaCorretamente() {
        // Cenario
        Clube flamengo = new Clube();
        flamengo.setId(1);
        flamengo.setNome("Flamengo");

        Jogador gabigol = new Jogador();
        gabigol.setId(1);
        gabigol.setNomeCompleto("Gabriel Barbosa");
        gabigol.setApelido("Gabigol");
        gabigol.setDataNascimento(LocalDate.of(1996, 8, 30));
        gabigol.setPosicao("Atacante");
        gabigol.setClube(flamengo);

        // Stat 1: Brasileirão 2025 (10 gols, 2 assistências)
        Competicao brasileirao = new Competicao();
        brasileirao.setNome("Brasileirão");
        brasileirao.setTemporada("2025");
        brasileirao.setTipoCompeticao(TipoCompeticao.PONTOS_CORRIDOS);

        EstatisticasJogadorCompeticao stat1 = new EstatisticasJogadorCompeticao();
        stat1.setCompeticao(brasileirao);
        stat1.setJogador(gabigol);
        stat1.setGols(10);
        stat1.setAssistencias(2);

        // Stat 2: Libertadores 2025 (5 gols, 1 assistência)
        Competicao libertadores = new Competicao();
        libertadores.setNome("Libertadores");
        libertadores.setTemporada("2025");
        libertadores.setTipoCompeticao(TipoCompeticao.MATA_MATA);

        EstatisticasJogadorCompeticao stat2 = new EstatisticasJogadorCompeticao();
        stat2.setCompeticao(libertadores);
        stat2.setJogador(gabigol);
        stat2.setGols(5);
        stat2.setAssistencias(1);

        gabigol.setEstatisticas(Arrays.asList(stat1, stat2));

        when(jogadorRepository.findAll()).thenReturn(List.of(gabigol));

        // Acao
        List<JogadorResponseDTO> resultado = jogadorService.listarJogadores();

        // Verificacao
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        JogadorResponseDTO dto = resultado.get(0);

        // 1. Verifica totais
        assertEquals(15, dto.golsTotais(), "Total de gols deve ser 15 (10 + 5)");
        assertEquals(3, dto.assistenciasTotais(), "Total de assistências deve ser 3 (2 + 1)");

        // 2. Verifica agrupamento por temporada
        List<EstatisticaTemporadaDTO> statsPorTemporada = dto.estatisticasPorTemporada();
        assertNotNull(statsPorTemporada);
        assertEquals(1, statsPorTemporada.size(), "Deve haver apenas 1 entrada para a temporada 2025");

        EstatisticaTemporadaDTO temporada2025 = statsPorTemporada.get(0);
        assertEquals("2025", temporada2025.temporada());
        assertEquals(15, temporada2025.totalGols(), "Gols da temporada 2025 devem ser 15");
        assertEquals(3, temporada2025.totalAssistencias(), "Assistências da temporada 2025 devem ser 3");
    }
}
