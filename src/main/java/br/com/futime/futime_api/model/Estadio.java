package br.com.futime.futime_api.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Estadio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cidade;
    private Double latitude;
    private Double longitude;
    // Agora temos uma tabela estadio independente, que pode conter todos os estádios, com ou sem clube associado.
    // Consultar estádios gerais (sem clube): Agora podemos ter registros na tabela estadio que não estão associados a nenhum clube. Podemos buscá-los com uma consulta específica no repositório de estádios.
}
