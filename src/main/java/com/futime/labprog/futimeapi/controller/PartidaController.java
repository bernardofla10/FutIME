package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.PartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.PartidaResponseDTO;
import com.futime.labprog.futimeapi.service.PartidaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/partidas")
@Tag(name = "Partidas", description = "Gerenciamento de partidas")
public class PartidaController {

    private final PartidaService partidaService;

    public PartidaController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }

    @GetMapping
    public List<PartidaResponseDTO> listarTodos() {
        return partidaService.listarPartidas();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartidaResponseDTO criar(@RequestBody @Valid PartidaRequestDTO dto) {
        return partidaService.criarPartida(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponseDTO> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(partidaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartidaResponseDTO> atualizar(@PathVariable Integer id, @RequestBody PartidaRequestDTO dto) {
        return ResponseEntity.ok(partidaService.atualizarPartida(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        partidaService.deletarPartida(id);
        return ResponseEntity.noContent().build();
    }
}
