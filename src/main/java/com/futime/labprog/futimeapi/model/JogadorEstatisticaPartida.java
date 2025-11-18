package com.futime.labprog.futimeapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "jogador_estatistica_partida")
public class JogadorEstatisticaPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partida_id", nullable = false)
    private Partida partida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jogador_id", nullable = false)
    private Jogador jogador;

    @Column(name = "minutos_jogados", nullable = false)
    private int minutosJogados;

    @Column(name = "cartao_amarelo", nullable = false)
    private boolean cartaoAmarelo;

    @Column(name = "cartao_vermelho", nullable = false)
    private boolean cartaoVermelho;

    @Column(name = "titular", nullable = false)
    private boolean titular;

    @Column(nullable = false)
    private int gols;

    @Column(nullable = false)
    private int assistencias;

    @Column // nullable
    private Integer defesa;

    @Column(name = "finalizacoes", nullable = false)
    private int finalizacoes;

    @Column(name = "chutes_a_gol", nullable = false)
    private int chutesAGol;

    @Column(nullable = false)
    private int desarmes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public JogadorEstatisticaPartida() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public void setJogador(Jogador jogador) {
        this.jogador = jogador;
    }

    public int getMinutosJogados() {
        return minutosJogados;
    }

    public void setMinutosJogados(int minutosJogados) {
        this.minutosJogados = minutosJogados;
    }

    public boolean isCartaoAmarelo() {
        return cartaoAmarelo;
    }

    public void setCartaoAmarelo(boolean cartaoAmarelo) {
        this.cartaoAmarelo = cartaoAmarelo;
    }

    public boolean isCartaoVermelho() {
        return cartaoVermelho;
    }

    public void setCartaoVermelho(boolean cartaoVermelho) {
        this.cartaoVermelho = cartaoVermelho;
    }

    public boolean isTitular() {
        return titular;
    }

    public void setTitular(boolean titular) {
        this.titular = titular;
    }

    public int getGols() {
        return gols;
    }

    public void setGols(int gols) {
        this.gols = gols;
    }

    public int getAssistencias() {
        return assistencias;
    }

    public void setAssistencias(int assistencias) {
        this.assistencias = assistencias;
    }

    public Integer getDefesa() {
        return defesa;
    }

    public void setDefesa(Integer defesa) {
        this.defesa = defesa;
    }

    public int getFinalizacoes() {
        return finalizacoes;
    }

    public void setFinalizacoes(int finalizacoes) {
        this.finalizacoes = finalizacoes;
    }

    public int getChutesAGol() {
        return chutesAGol;
    }

    public void setChutesAGol(int chutesAGol) {
        this.chutesAGol = chutesAGol;
    }

    public int getDesarmes() {
        return desarmes;
    }

    public void setDesarmes(int desarmes) {
        this.desarmes = desarmes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}


