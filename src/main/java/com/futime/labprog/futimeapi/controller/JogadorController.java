package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.JogadorRequestDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;
import com.futime.labprog.futimeapi.service.JogadorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jogadores")
public class JogadorController {

    private final JogadorService jogadorService;

    // Injeção da Interface via construtor
    public JogadorController(JogadorService jogadorService) {
        this.jogadorService = jogadorService;
    }

    @GetMapping
    public List<JogadorResponseDTO> listarTodos() {
        return jogadorService.listarJogadores();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JogadorResponseDTO criarJogador(@RequestBody JogadorRequestDTO novoJogadorDTO) {
        return jogadorService.criarJogador(novoJogadorDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogadorResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return jogadorService.buscarJogadorPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogadorResponseDTO> atualizarJogador(@PathVariable("id") Integer id, @RequestBody JogadorRequestDTO jogadorDTO) {
        return jogadorService.atualizarJogador(id, jogadorDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarJogador(@PathVariable("id") Integer id) {
        if (jogadorService.deletarJogador(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}