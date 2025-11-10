package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.EstatisticasRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticasResponseDTO;
import com.futime.labprog.futimeapi.service.EstatisticasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estatisticas") // Prefixo base para endpoints de estatísticas diretos
public class EstatisticasController {

    private final EstatisticasService estatisticasService;

    public EstatisticasController(EstatisticasService estatisticasService) {
        this.estatisticasService = estatisticasService;
    }

    /**
     * Endpoint "Upsert" (Cria ou Atualiza) para estatísticas.
     * RESTful: POST /jogadores/{jogadorId}/competicoes/{competicaoId}/estatisticas
     * Indica a criação de um sub-recurso.
     */
    @PostMapping("/jogadores/{jogadorId}/competicoes/{competicaoId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EstatisticasResponseDTO salvarEstatisticas(
            @PathVariable("jogadorId") Integer jogadorId, // <-- CORRIGIDO
            @PathVariable("competicaoId") Integer competicaoId, // <-- CORRIGIDO
            @RequestBody EstatisticasRequestDTO requestDTO) {
        
        return estatisticasService.salvarEstatisticas(jogadorId, competicaoId, requestDTO);
    }

    /**
     * Endpoint para listar todas as estatísticas de um jogador.
     * RESTful: GET /jogadores/{jogadorId}/estatisticas
     */
    @GetMapping("/jogadores/{jogadorId}")
    public List<EstatisticasResponseDTO> listarEstatisticasPorJogador(
            @PathVariable("jogadorId") Integer jogadorId) { // <-- CORRIGIDO
        
        return estatisticasService.listarEstatisticasPorJogador(jogadorId);
    }

    /**
     * Endpoint para buscar um registro de estatística pelo seu ID único.
     * RESTful: GET /estatisticas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EstatisticasResponseDTO> buscarEstatisticaPorId(@PathVariable("id") Integer id) {
        return estatisticasService.buscarEstatisticaPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para deletar um registro de estatística pelo seu ID único.
     * RESTful: DELETE /estatisticas/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletarEstatistica(@PathVariable("id") Integer id) {
        if (estatisticasService.deletarEstatistica(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}