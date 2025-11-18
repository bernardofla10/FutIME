package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.PartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.PartidaResponseDTO;
import com.futime.labprog.futimeapi.service.PartidaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partidas")
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
    public PartidaResponseDTO criar(@RequestBody PartidaRequestDTO dto) {
        return partidaService.criarPartida(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartidaResponseDTO> buscarPorId(@PathVariable Integer id) {
        return partidaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartidaResponseDTO> atualizar(@PathVariable Integer id, @RequestBody PartidaRequestDTO dto) {
        return partidaService.atualizarPartida(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletar(@PathVariable Integer id) {
        return partidaService.deletarPartida(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}


