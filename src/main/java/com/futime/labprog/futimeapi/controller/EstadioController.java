package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.EstadioRequestDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.service.EstadioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estadios")
@Tag(name = "Estádios", description = "Gerenciamento de estádios")
public class EstadioController {

    private final EstadioService estadioService;

    public EstadioController(EstadioService estadioService) {
        this.estadioService = estadioService;
    }

    @GetMapping
    public List<EstadioResponseDTO> listarTodos() {
        return estadioService.listarEstadios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EstadioResponseDTO criarEstadio(@RequestBody EstadioRequestDTO novoEstadioDTO) {
        return estadioService.criarEstadio(novoEstadioDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstadioResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return estadioService.buscarEstadioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstadioResponseDTO> atualizarEstadio(@PathVariable("id") Integer id,
            @RequestBody EstadioRequestDTO estadioDTO) {
        return estadioService.atualizarEstadio(id, estadioDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEstadio(@PathVariable("id") Integer id) {
        if (estadioService.deletarEstadio(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
