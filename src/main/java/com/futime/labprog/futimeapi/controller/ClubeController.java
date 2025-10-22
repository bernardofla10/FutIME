package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.ClubeRequestDTO;
import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.service.ClubeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubes")
public class ClubeController {

    private final ClubeService clubeService;

    // Injeção da *Interface* (Abstração)
    public ClubeController(ClubeService clubeService) {
        this.clubeService = clubeService;
    }

    @GetMapping
    public List<ClubeResponseDTO> listarTodos() {
        return clubeService.listarClubes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClubeResponseDTO criarClube(@RequestBody ClubeRequestDTO novoClubeDTO) {
        return clubeService.criarClube(novoClubeDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClubeResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return clubeService.buscarClubePorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClubeResponseDTO> atualizarClube(@PathVariable("id") Integer id, @RequestBody ClubeRequestDTO clubeDTO) {
        return clubeService.atualizarClube(id, clubeDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarClube(@PathVariable("id") Integer id) {
        if (clubeService.deletarClube(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}