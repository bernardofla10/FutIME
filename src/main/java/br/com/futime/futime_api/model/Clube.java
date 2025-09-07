package br.com.futime.futime_api.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Entity //  Diz ao JPA/Hibernate: "Esta classe não é uma classe comum, ela representa uma tabela no banco de dados". Por padrão, o nome da tabela será o mesmo nome da classe, em minúsculas (clube).
@Data   // É um atalho do Lombok. Em vez de escrevermos manualmente os métodos getters, setters, toString, etc., o Lombok os gera para nós, deixando o código super limpo.
public class Clube {
    
    @Id // Marca o campo id como a chave primária (Primary Key) da tabela.
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Instrui o banco de dados a gerar o valor para o id automaticamente toda vez que um novo clube for inserido. A estratégia IDENTITY é ideal para o PostgreSQL.
    private Long id;

    private String nome;
    private String pais;
    private Integer anoFundacao;

    @OneToOne
    @JoinColumn(name = "estadio_id", referencedColumnName = "id")
    private Estadio estadio;
    // Nem todos os clubes possuem estádios: A relação @OneToOne permite que o campo estadio_id na tabela clube seja nulo (NULL).
    // Consultar o estádio de um clube: Basta acessar o campo clube.getEstadio(). Se for nulo, o clube não tem estádio próprio.
}
