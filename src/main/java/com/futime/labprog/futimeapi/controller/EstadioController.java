package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.repository.EstadioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para gerenciar a entidade Estadio.
 * Expõe os endpoints da API para as operações de CRUD.
 */
@RestController // combina @Controller e @ResponseBody: classe controlador capaz de receber requisições HTTP.
// e retorno de cada método convertido diretamente para JSON e enviado no corpo da resposta HTTP.

@RequestMapping("/estadios") // Define um prefixo de URL para todos os endpoints dentro desta classe. 
// Isso significa que o método listarTodos responderá em /estadios, e o criarEstadio também.

public class EstadioController {
    
    @Autowired // Injeção de Dependência. "Encontre o Bean do tipo EstadioRepository e injete uma instância dele na variável".
    private EstadioRepository estadioRepository;

    /**
     * Endpoint para listar todos os estádios cadastrados.
     * Mapeado para requisições HTTP GET em /estadios.
     *
     * @return Uma lista de todos os estádios.
     */
    @GetMapping // Não passamos nenhum valor, então assume a URL base da classe, ou seja, GET /estadios.
    public List<Estadio> listarTodos() { // método executado quando a requisição chega
        return estadioRepository.findAll(); // método herdado de JpaRepository que retorna todos os registros da tabela estadios.
    }

    /**
     * Endpoint para cadastrar um novo estádio.
     * Mapeado para requisições HTTP POST em /estadios.
     *
     * @param novoEstadio O objeto Estadio enviado no corpo da requisição.
     * @return O estádio salvo, incluindo o ID gerado pelo banco.
     */
    @PostMapping // POST /estadios
    @ResponseStatus(HttpStatus.CREATED) // Por padrão, o Spring retorna HTTP status 200 OK para requisições bem sucedidas.
    // A boa prática em APIs REST é retornar 201 Created quando um recurso novo é criado com sucesso.
    public Estadio criarEstadio(@RequestBody Estadio novoEstadio) { // Pega o corpo da requisição HTTP (JSON), converte em um objeto Estadio e passa para o método
        return estadioRepository.save(novoEstadio); // salva no banco de dados e retorna a nova entidade salva.
    }
    
    
}
