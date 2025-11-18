package com.futime.labprog.futimeapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Representa a entidade Clube no banco de dados.
 * Cada instância desta classe corresponde a uma linha na tabela 'clubes'.
 */
@Entity // Diz ao JPA que esta classe é uma tabela
@Table(name = "clubes") // Mapeia para a tabela "clubes"
public class Clube {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private String sigla; // Ex: FLA, SAO, COR
    private String cidade;
    private String pais;

    /**
     * Relacionamento Um-para-Um com Estadio.
     * Um clube tem um estádio principal.
     */
    @OneToOne(fetch = FetchType.LAZY) // (fetch = FetchType.LAZY) é uma boa prática de performance.
                                      // Só carrega o Estadio do banco quando
                                      // explicitamente chamarmos clube.getEstadio().
    @JoinColumn(name = "estadio_id", referencedColumnName = "id") // Define a chave estrangeira
                                                                // na tabela 'clubes'.
    private Estadio estadio;

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public Clube() {
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

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public Estadio getEstadio() {
        return estadio;
    }

    public void setEstadio(Estadio estadio) {
        this.estadio = estadio;
    }

    // TODO: Implementar equals() e hashCode() baseados apenas no 'id',
    // assim como fizemos em Estadio.java.
}
