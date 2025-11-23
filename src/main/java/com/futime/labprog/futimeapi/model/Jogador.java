package com.futime.labprog.futimeapi.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jogadores")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    private String apelido;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    private String posicao;

    @Column(name = "valor_de_mercado")
    private Double valorDeMercado;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clube_id")
    private Clube clube;

    @OneToMany(mappedBy = "jogador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EstatisticasJogadorCompeticao> estatisticas = new ArrayList<>();

    @OneToMany(mappedBy = "jogador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JogadorEstatisticaPartida> estatisticasPartida = new ArrayList<>();

    @ManyToMany(mappedBy = "jogadoresObservados")
    private List<Usuario> usuariosObservadores = new ArrayList<>();

    public Jogador() {
    }

    public Jogador(String nomeCompleto, String apelido, LocalDate dataNascimento, String posicao, Clube clube) {
        this.nomeCompleto = nomeCompleto;
        this.apelido = apelido;
        this.dataNascimento = dataNascimento;
        this.posicao = posicao;
        this.clube = clube;
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getPosicao() {
        return posicao;
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao;
    }

    public Double getValorDeMercado() {
        return valorDeMercado;
    }

    public void setValorDeMercado(Double valorDeMercado) {
        this.valorDeMercado = valorDeMercado;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Clube getClube() {
        return clube;
    }

    public void setClube(Clube clube) {
        this.clube = clube;
    }

    public List<EstatisticasJogadorCompeticao> getEstatisticas() {
        return estatisticas;
    }

    public void setEstatisticas(List<EstatisticasJogadorCompeticao> estatisticas) {
        this.estatisticas = estatisticas;
    }

    public List<JogadorEstatisticaPartida> getEstatisticasPartida() {
        return estatisticasPartida;
    }

    public void setEstatisticasPartida(List<JogadorEstatisticaPartida> estatisticasPartida) {
        this.estatisticasPartida = estatisticasPartida;
    }

    public List<Usuario> getUsuariosObservadores() {
        return usuariosObservadores;
    }

    public void setUsuariosObservadores(List<Usuario> usuariosObservadores) {
        this.usuariosObservadores = usuariosObservadores;
    }
}
