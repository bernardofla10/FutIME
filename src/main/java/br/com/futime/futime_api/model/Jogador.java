package br.com.futime.futime_api.model;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String posicao;
    private LocalDate dataNascimento;
    private Double valorDeMercado;

    @ManyToOne
    @JoinColumn(name = "clube_atual_id")
    private Clube clubeAtual;
    // Consultar jogadores de um clube: Agora podemos buscar no banco todos os jogadores onde o clube_atual_id seja o ID do clube desejado.
    // Consultar o clube de um jogador: Cada objeto Jogador terá um campo clubeAtual que nos dará essa informação diretamente.


}