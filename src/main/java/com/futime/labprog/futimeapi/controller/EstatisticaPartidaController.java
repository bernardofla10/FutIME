package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.EstatisticaPartidaRequestDTO;
import com.futime.labprog.futimeapi.dto.EstatisticaPartidaResponseDTO;
import com.futime.labprog.futimeapi.service.EstatisticaPartidaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/estatisticas-partida")
@Tag(name = "Estatísticas de Partidas", description = "Gerenciamento de estatísticas de partidas")
public class EstatisticaPartidaController {

    private final EstatisticaPartidaService service;

    public EstatisticaPartidaController(EstatisticaPartidaService service) {
        this.service = service;
    }

    @PostMapping("/jogadores/{jogadorId}/partidas/{partidaId}/estatisticas")
    @ResponseStatus(HttpStatus.CREATED)
    public EstatisticaPartidaResponseDTO salvar(@PathVariable Integer jogadorId,
            @PathVariable Integer partidaId,
            @RequestBody EstatisticaPartidaRequestDTO dto) {
        return service.salvar(jogadorId, partidaId, dto);
    }

    @GetMapping("/jogadores/{jogadorId}/partidas/estatisticas")
    public List<EstatisticaPartidaResponseDTO> listarPorJogador(@PathVariable Integer jogadorId) {
        return service.listarPorJogador(jogadorId);
    }

    @GetMapping("/estatisticas-partida/{id}")
    public ResponseEntity<EstatisticaPartidaResponseDTO> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/estatisticas-partida/{id}")
    public ResponseEntity<Object> deletar(@PathVariable Integer id) {
        return service.deletar(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
