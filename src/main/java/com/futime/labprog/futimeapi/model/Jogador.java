package com.futime.labprog.futimeapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate; // Para data de nascimento

/**
 * Representa a entidade Jogador no banco de dados.
 */
@Entity
@Table(name = "jogadores")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nomeCompleto;
    private String apelido;
    private LocalDate dataNascimento;
    private String posicao; // Ex: Atacante, Goleiro, Meio-campo

    /**
     * Relacionamento Muitos-para-Um com Clube.
     * Muitos jogadores pertencem a um clube.
     */
    @ManyToOne(fetch = FetchType.LAZY) // LAZY continua sendo boa prática
    @JoinColumn(name = "clube_id", referencedColumnName = "id") // Chave estrangeira na tabela 'jogadores'
    private Clube clube;

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public Jogador() {
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getPosicao() { return posicao; }
    public void setPosicao(String posicao) { this.posicao = posicao; }
    public Clube getClube() { return clube; }
    public void setClube(Clube clube) { this.clube = clube; }

    // TODO: Implementar equals() e hashCode() baseados apenas no 'id'
    //.
}
