package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.EstadioRequestDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.service.EstadioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    
    private final EstadioService estadioService;

    // Injeção de Dependência via Construtor
    public EstadioController(EstadioService estadioService) {
        this.estadioService = estadioService;
    }

    /**
     * Endpoint para listar todos os estádios cadastrados.
     * Mapeado para requisições HTTP GET em /estadios.
     *
     * @return Uma lista de todos os estádios.
     */
    @GetMapping // Não passamos nenhum valor, então assume a URL base da classe, ou seja, GET /estadios.
    public List<EstadioResponseDTO> listarTodos() {
        return estadioService.listarEstadios();
    }

    /**
     * Endpoint para cadastrar um novo estádio.
     * Mapeado para requisições HTTP POST em /estadios.
     *
     * @param novoEstadioDTO O objeto EstadioRequestDTO enviado no corpo da requisição.
     * @return O estádio salvo, incluindo o ID gerado pelo banco.
     */
    @PostMapping // POST /estadios
    @ResponseStatus(HttpStatus.CREATED) // Por padrão, o Spring retorna HTTP status 200 OK para requisições bem sucedidas.
    // A boa prática em APIs REST é retornar 201 Created quando um recurso novo é criado com sucesso.
    public EstadioResponseDTO criarEstadio(@RequestBody EstadioRequestDTO novoEstadioDTO) {
        return estadioService.criarEstadio(novoEstadioDTO); // salva no banco de dados.
    }

    /**
     * Endpoint para buscar um estádio específico pelo seu ID.
     * Mapeado para requisições HTTP GET em /estadios/{id}
     *
     * @param id O ID do estádio passado na URL.
     * @return O estádio encontrado ou um status 404 Not Found.
     */
    @GetMapping("/{id}") // GET /estadios/{id}
    public ResponseEntity<EstadioResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return estadioService.buscarEstadioPorId(id)
                .map(ResponseEntity::ok) // Se encontrou, retorna 200 OK com o DTO
                .orElse(ResponseEntity.notFound().build()); // Se não, retorna 404
    }
    // ResponseEntity é o ideal para representar a resposta HTTP completa, não apenas o conteúdo dela (Body + Status Code + Headers).
    
    /**
     * Endpoint para atualizar um estádio existente.
     * Mapeado para requisições HTTP PUT em /estadios/{id}
     *
     * @param id O ID do estádio a ser atualizado (da URL).
     * @param estadioDetalhes O objeto Estadio com os novos dados (do corpo da requisição).
     * @return O estádio atualizado ou um status 404 Not Found.
     */
    @PutMapping("/{id}") // PUT /estadios/{id}
    public ResponseEntity<EstadioResponseDTO> atualizarEstadio(@PathVariable("id") Integer id, @RequestBody EstadioRequestDTO estadioDTO) {
        return estadioService.atualizarEstadio(id, estadioDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Endpoint para deletar um estádio pelo seu ID.
     * Mapeado para requisições HTTP DELETE em /estadios/{id}
     *
     * @param id O ID do estádio a ser deletado (da URL).
     * @return Um status 204 No Content (sucesso) ou 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEstadio(@PathVariable("id") Integer id) {
        if (estadioService.deletarEstadio(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content: boa prática para uma operação DELETE bem sucedida.
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}
