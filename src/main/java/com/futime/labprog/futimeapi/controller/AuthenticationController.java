package com.futime.labprog.futimeapi.controller;

import com.futime.labprog.futimeapi.dto.LoginDTO;
import com.futime.labprog.futimeapi.dto.RegisterDTO;
import com.futime.labprog.futimeapi.dto.UsuarioResponseDTO;
import com.futime.labprog.futimeapi.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Registro e autenticação de usuários")
public class AuthenticationController {

    private final UsuarioService usuarioService;

    public AuthenticationController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        UsuarioResponseDTO novoUsuario = usuarioService.registerUser(registerDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponseDTO> login(@RequestBody @Valid LoginDTO loginDTO) {
        UsuarioResponseDTO usuario = usuarioService.login(loginDTO);
        return ResponseEntity.ok(usuario);
    }
}
