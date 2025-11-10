package com.futime.labprog.futimeapi.model;

import jakarta.persistence.*;

/**
 * Entidade que armazena a performance (estatísticas) de um Jogador
 * em uma Competição específica. Esta é a nossa fonte da verdade.
 */
@Entity
@Table(name = "jogador_estatisticas_competicao")
public class EstatisticasJogadorCompeticao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relação N-1: Qual jogador?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_id", nullable = false)
    private Jogador jogador;

    // Relação N-1: Em qual competição?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competicao_id", nullable = false)
    private Competicao competicao;

    // Os atributos atômicos
    private int gols;
    private int assistencias;
    private int jogosDisputados;

    public EstatisticasJogadorCompeticao() {
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Jogador getJogador() { return jogador; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }
    public Competicao getCompeticao() { return competicao; }
    public void setCompeticao(Competicao competicao) { this.competicao = competicao; }
    public int getGols() { return gols; }
    public void setGols(int gols) { this.gols = gols; }
    public int getAssistencias() { return assistencias; }
    public void setAssistencias(int assistencias) { this.assistencias = assistencias; }
    public int getJogosDisputados() { return jogosDisputados; }
    public void setJogosDisputados(int jogosDisputados) { this.jogosDisputados = jogosDisputados; }
    
    // TODO: Implementar equals() e hashCode()
}