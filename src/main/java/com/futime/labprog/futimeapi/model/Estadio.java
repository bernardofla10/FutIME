package com.futime.labprog.futimeapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Representa a entidade Estadio no banco de dados.
 * Cada instância desta classe corresponde a uma linha na tabela 'estadios'.
 */
@Entity // Diz ao Spring Data JPA que a classe Estadio é uma entidade e deve ser mapeada para uma tabela no banco de dados.
@Table(name = "estadios") // Cria uma tabela no banco de dados chamada estadios.
public class Estadio {

    @Id // Atributo id será a chave primária da tabela.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configura a geração da chave primária. IDENTITY delega ao banco de dados a tarefa de auto-incrementar o ID a cada novo registro.
    private Integer id;

    @Column(nullable = false, length = 100) // Mapeia os atributos para colunas na tabela. nullable = false para garantir que são obrigatórios e length para definir o tamanho máximo.
    private String nome;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf; // Unidade Federativa (ex: RJ, SP)

    /**
     * Construtor padrão exigido pelo JPA.
     */
    public Estadio() {
    }

    /**
     * Construtor para criar uma instância de Estadio com dados iniciais.
     *
     * @param nome O nome do estádio.
     * @param cidade A cidade onde o estádio está localizado.
     * @param uf A sigla do estado (UF).
     */
    public Estadio(String nome, String cidade, String uf) {
        this.nome = nome;
        this.cidade = cidade;
        this.uf = uf;
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

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }


    // Para uma entidade que representa uma linha em um banco de dados, a sua identidade única é a sua chave primária (o ID). Portanto, dois objetos Estadio são "iguais" se eles têm o mesmo id.

    @Override // Indica que estamos sobrescrevendo o método equals padrão da classe Object do Java.
    public boolean equals(Object o) { // // A assinatura do método. Ele recebe um objeto qualquer (o) e retorna true (se forem iguais) ou false.
        if (this == o) return true; // As variáveis this e o apontam para o mesmíssimo objeto na memória?". Se sim, eles são obviamente iguais, e já podemos retornar true sem fazer mais nada.
        if (o == null || getClass() != o.getClass()) return false; // O objeto o é nulo? Se for, não pode ser igual a this. O objeto o é de uma classe diferente? Se this é um Estadio e o é um Jogador, eles nunca poderão ser iguais.
        Estadio estadio = (Estadio) o; // Agora, sabemos que o é um objeto Estadio não-nulo, podemos convertê-lo para o tipo Estadio.
        return Objects.equals(id, estadio.id); // Dois objetos Estadio são considerados iguais se, e somente se, seus IDs forem iguais.
    }


    // Se dois objetos são iguais de acordo com o método equals(), eles OBRIGATORIAMENTE devem ter o mesmo hashCode.

    @Override 
    public int hashCode() { // Se dois objetos são iguais de acordo com o método equals(), eles OBRIGATORIAMENTE devem ter o mesmo hashCode.
        return Objects.hash(id); // calcular um hashCode a partir do id. Ele simplesmente pega o id e gera a "impressão digital" numérica para ele.
    }
    // Isso garante que se estadioA.equals(estadioB) for true, então estadioA.hashCode() será igual a estadioB.hashCode().
}
