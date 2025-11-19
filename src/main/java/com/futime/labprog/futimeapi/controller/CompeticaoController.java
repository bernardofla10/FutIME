package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.CompeticaoRequestDTO;
import com.futime.labprog.futimeapi.dto.CompeticaoResponseDTO;
import com.futime.labprog.futimeapi.service.CompeticaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/competicoes")
@Tag(name = "Competições", description = "Gerenciamento de competições")
public class CompeticaoController {

    private final CompeticaoService competicaoService;

    public CompeticaoController(CompeticaoService competicaoService) {
        this.competicaoService = competicaoService;
    }

    @GetMapping
    public List<CompeticaoResponseDTO> listarTodos() {
        return competicaoService.listarCompeticoes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompeticaoResponseDTO criarCompeticao(@RequestBody @Valid CompeticaoRequestDTO novaCompeticaoDTO) {
        return competicaoService.criarCompeticao(novaCompeticaoDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompeticaoResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(competicaoService.buscarCompeticaoPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompeticaoResponseDTO> atualizarCompeticao(@PathVariable("id") Integer id,
            @RequestBody @Valid CompeticaoRequestDTO competicaoDTO) {
        return ResponseEntity.ok(competicaoService.atualizarCompeticao(id, competicaoDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarCompeticao(@PathVariable("id") Integer id) {
        competicaoService.deletarCompeticao(id);
        return ResponseEntity.noContent().build();
    }
}
