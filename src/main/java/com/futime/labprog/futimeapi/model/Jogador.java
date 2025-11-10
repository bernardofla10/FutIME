package com.futime.labprog.futimeapi.model;

import jakarta.persistence.*; // Importação completa
import java.time.LocalDate;
import java.util.ArrayList; // ADICIONADO
import java.util.List;

@Entity
@Table(name = "jogadores")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nomeCompleto;
    private String apelido;
    private LocalDate dataNascimento;
    private String posicao;

    // NOVO CAMPO: Atributo simples
    private Double valorDeMercado; // Usar Double ou BigDecimal para valores monetários

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clube_id", referencedColumnName = "id")
    private Clube clube;

    // NOVA RELAÇÃO: Um Jogador tem Muitas folhas de estatísticas
    /**
     * Relacionamento Um-para-Muitos com as estatísticas do jogador.
     * 'mappedBy = "jogador"' indica que a entidade EstatisticasJogadorCompeticao
     * é a "dona" deste relacionamento (ela possui a chave estrangeira).
     * cascade = CascadeType.ALL: Se um jogador for deletado, suas estatísticas
     * relacionadas também serão.
     * orphanRemoval = true: Se uma estatística for removida da lista deste jogador,
     * ela será deletada do banco.
     */
    @OneToMany(mappedBy = "jogador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EstatisticasJogadorCompeticao> estatisticas = new ArrayList<>(); // ATUALIZADO

    public Jogador() {
    }

    // Getters e Setters atualizados/adicionados
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

    // Getter/Setter para valorDeMercado
    public Double getValorDeMercado() { return valorDeMercado; }
    public void setValorDeMercado(Double valorDeMercado) { this.valorDeMercado = valorDeMercado; }

    // Getter/Setter para a nova coleção de estatísticas
    public List<EstatisticasJogadorCompeticao> getEstatisticas() { return estatisticas; }
    public void setEstatisticas(List<EstatisticasJogadorCompeticao> estatisticas) { this.estatisticas = estatisticas; }
    
    // TODO: Implementar equals() e hashCode()
}
