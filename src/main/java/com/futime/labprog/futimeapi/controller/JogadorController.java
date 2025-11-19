package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.JogadorRequestDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;
import com.futime.labprog.futimeapi.service.JogadorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jogadores")
@Tag(name = "Jogadores", description = "Gerenciamento de jogadores")
public class JogadorController {

    private final JogadorService jogadorService;

    public JogadorController(JogadorService jogadorService) {
        this.jogadorService = jogadorService;
    }

    @GetMapping
    public List<JogadorResponseDTO> listarTodos() {
        return jogadorService.listarJogadores();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JogadorResponseDTO criarJogador(@RequestBody @Valid JogadorRequestDTO novoJogadorDTO) {
        return jogadorService.criarJogador(novoJogadorDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JogadorResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(jogadorService.buscarJogadorPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JogadorResponseDTO> atualizarJogador(@PathVariable("id") Integer id,
            @RequestBody @Valid JogadorRequestDTO jogadorDTO) {
        return ResponseEntity.ok(jogadorService.atualizarJogador(id, jogadorDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarJogador(@PathVariable("id") Integer id) {
        jogadorService.deletarJogador(id);
        return ResponseEntity.noContent().build();
    }
}