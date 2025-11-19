package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.EstatisticasRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticasResponseDTO;
import com.futime.labprog.futimeapi.service.EstatisticasService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estatisticas")
@Tag(name = "Estatísticas", description = "Consultas de estatísticas gerais")
public class EstatisticasController {

    private final EstatisticasService estatisticasService;

    public EstatisticasController(EstatisticasService estatisticasService) {
        this.estatisticasService = estatisticasService;
    }

    @PostMapping("/jogadores/{jogadorId}/competicoes/{competicaoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EstatisticasResponseDTO salvarEstatisticas(
            @PathVariable("jogadorId") Integer jogadorId,
            @PathVariable("competicaoId") Integer competicaoId,
            @RequestBody EstatisticasRequestDTO requestDTO) {

        return estatisticasService.salvarEstatisticas(jogadorId, competicaoId, requestDTO);
    }

    @GetMapping("/jogadores/{jogadorId}")
    public List<EstatisticasResponseDTO> listarEstatisticasPorJogador(
            @PathVariable("jogadorId") Integer jogadorId) {

        return estatisticasService.listarEstatisticasPorJogador(jogadorId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstatisticasResponseDTO> buscarEstatisticaPorId(@PathVariable("id") Integer id) {
        return estatisticasService.buscarEstatisticaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEstatistica(@PathVariable("id") Integer id) {
        if (estatisticasService.deletarEstatistica(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}