package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.CompeticaoRequestDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoResponseDTO;
import com.futime.labprog.futimeapi.service.CompeticaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competicoes")
public class CompeticaoController {

    private final CompeticaoService competicaoService;

    // Injeção da *Interface* (Abstração)
    public CompeticaoController(CompeticaoService competicaoService) {
        this.competicaoService = competicaoService;
    }

    @GetMapping
    public List<CompeticaoResponseDTO> listarTodos() {
        return competicaoService.listarCompeticoes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompeticaoResponseDTO criarCompeticao(@RequestBody CompeticaoRequestDTO novaCompeticaoDTO) {
        return competicaoService.criarCompeticao(novaCompeticaoDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompeticaoResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return competicaoService.buscarCompeticaoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompeticaoResponseDTO> atualizarCompeticao(@PathVariable("id") Integer id, @RequestBody CompeticaoRequestDTO competicaoDTO) {
        return competicaoService.atualizarCompeticao(id, competicaoDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarCompeticao(@PathVariable("id") Integer id) {
        if (competicaoService.deletarCompeticao(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
