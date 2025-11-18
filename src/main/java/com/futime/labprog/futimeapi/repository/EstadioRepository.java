package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Estadio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Interface de repositório para a entidade Estadio.
 * Fornece os métodos de CRUD (Create, Read, Update, Delete) através da
 * herança de JpaRepository.
 */
@Repository // Esta anotação informa ao Spring que esta interface é um "Bean" de repositório. Um Bean é um objeto criado e gerenciado pelo Spring (Spring faz tudo).
public interface EstadioRepository extends JpaRepository<Estadio, Integer> { // parâmetros: Estadio informa ao Spring qual é a entidade que este repositório vai gerenciar e Integer informa o tipo da PK da entidade.
    // Ao extender JpaRepository, a interface herda métodos prontos CRUD para interagir com o banco de dados.
    // herda save(), findById(), findAll(), deleteById(), etc.
}

// Por que utilizar uma interface e não uma classe normal?
// 1) Spring Data JPA foi projetado para funcionar assim.
// 2) Torna o sistema flexível pois não depende de uma implementação específica (Ex: A classe que usa o Repo, Controller, não depende do BD escolhido. Ou seja, posso trocar o Postgres por outro).
// 3) Como o Controller depende da interface EstadioRepository, durante os testes, podemos fazer uma implementação falsa dessa interface, sem se conectar a um banco de verdade (bem mais rápido)