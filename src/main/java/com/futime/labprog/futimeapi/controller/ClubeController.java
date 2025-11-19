package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.ClubeRequestDTO;
import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.service.ClubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubes")
@Tag(name = "Clubes", description = "Gerenciamento de clubes de futebol")
public class ClubeController {

    private final ClubeService clubeService;

    public ClubeController(ClubeService clubeService) {
        this.clubeService = clubeService;
    }

    @GetMapping
    @Operation(summary = "Listar todos os clubes", description = "Retorna uma lista com todos os clubes cadastrados")
    @ApiResponse(responseCode = "200", description = "Sucesso")
    public List<ClubeResponseDTO> listarTodos() {
        return clubeService.listarClubes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar um novo clube", description = "Cadastra um novo clube na base de dados")
    @ApiResponse(responseCode = "201", description = "Clube criado com sucesso")
    public ClubeResponseDTO criarClube(@RequestBody @Valid ClubeRequestDTO novoClubeDTO) {
        return clubeService.criarClube(novoClubeDTO);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar clube por ID", description = "Retorna os detalhes de um clube específico")
    @ApiResponse(responseCode = "200", description = "Clube encontrado")
    @ApiResponse(responseCode = "404", description = "Clube não encontrado")
    public ResponseEntity<ClubeResponseDTO> buscarPorId(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(clubeService.buscarClubePorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar clube", description = "Atualiza os dados de um clube existente")
    @ApiResponse(responseCode = "200", description = "Clube atualizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Clube não encontrado")
    public ResponseEntity<ClubeResponseDTO> atualizarClube(@PathVariable("id") Integer id,
            @RequestBody @Valid ClubeRequestDTO clubeDTO) {
        return ResponseEntity.ok(clubeService.atualizarClube(id, clubeDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar clube", description = "Remove um clube da base de dados")
    @ApiResponse(responseCode = "204", description = "Clube deletado com sucesso")
    @ApiResponse(responseCode = "404", description = "Clube não encontrado")
    public ResponseEntity<Void> deletarClube(@PathVariable("id") Integer id) {
        clubeService.deletarClube(id);
        return ResponseEntity.noContent().build();
    }
}