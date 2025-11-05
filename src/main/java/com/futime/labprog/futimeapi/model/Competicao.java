package com.futime.labprog.futimeapi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "competicoes")
public class Competicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String pais;

    @Column(nullable = false)
    private String continente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCompeticao tipoCompeticao;

    @Column(nullable = false)
    private String temporada;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "competicao_clube",
        joinColumns = @JoinColumn(name = "competicao_id"),
        inverseJoinColumns = @JoinColumn(name = "clube_id")
    )
    private List<Clube> clubes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Construtor padrão
    public Competicao() {
    }

    // Construtor com parâmetros
    public Competicao(String nome, String pais, String continente, TipoCompeticao tipoCompeticao, String temporada) {
        this.nome = nome;
        this.pais = pais;
        this.continente = continente;
        this.tipoCompeticao = tipoCompeticao;
        this.temporada = temporada;
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

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getContinente() {
        return continente;
    }

    public void setContinente(String continente) {
        this.continente = continente;
    }

    public TipoCompeticao getTipoCompeticao() {
        return tipoCompeticao;
    }

    public void setTipoCompeticao(TipoCompeticao tipoCompeticao) {
        this.tipoCompeticao = tipoCompeticao;
    }

    public String getTemporada() {
        return temporada;
    }

    public void setTemporada(String temporada) {
        this.temporada = temporada;
    }

    public List<Clube> getClubes() {
        return clubes;
    }

    public void setClubes(List<Clube> clubes) {
        this.clubes = clubes;
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
