package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.RegisterDTO;
import com.futime.labprog.futimeapi.dto.UsuarioResponseDTO;
import com.futime.labprog.futimeapi.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UsuarioService usuarioService;

    public AuthenticationController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@RequestBody RegisterDTO registerDTO) {
        UsuarioResponseDTO novoUsuario = usuarioService.registerUser(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }
}
