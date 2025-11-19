package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.UsuarioResponseDTO;
import com.futime.labprog.futimeapi.model.Usuario;
import com.futime.labprog.futimeapi.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Helper para pegar o usuário logado
    private Usuario getUsuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Usuario) authentication.getPrincipal();
    }

    @GetMapping("/perfil")
    public ResponseEntity<UsuarioResponseDTO> getPerfil() {
        Usuario usuario = getUsuarioLogado();
        // Buscamos novamente no serviço para garantir dados atualizados (lazy loading
        // etc)
        UsuarioResponseDTO perfil = usuarioService.buscarPerfil(usuario.getId());
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/meu-time/{clubeId}")
    public ResponseEntity<UsuarioResponseDTO> definirTimeCoracao(@PathVariable Integer clubeId) {
        Usuario usuario = getUsuarioLogado();
        UsuarioResponseDTO atualizado = usuarioService.definirClubeFavorito(usuario.getId(), clubeId);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/olheiro/{jogadorId}")
    public ResponseEntity<UsuarioResponseDTO> adicionarJogadorObservado(@PathVariable Integer jogadorId) {
        Usuario usuario = getUsuarioLogado();
        UsuarioResponseDTO atualizado = usuarioService.adicionarJogadorObservado(usuario.getId(), jogadorId);
        return ResponseEntity.ok(atualizado);
    }
}
