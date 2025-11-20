package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.UsuarioResponseDTO;
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.model.Usuario;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
import com.futime.labprog.futimeapi.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClubeRepository clubeRepository;

    @Mock
    private JogadorRepository jogadorRepository;

    @Mock
    private JogadorService jogadorService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    @DisplayName("Deve remover jogador observado com sucesso")
    void deveRemoverJogadorObservadoComSucesso() {
        // Cenario
        Integer usuarioId = 1;
        Integer jogadorId = 10;

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("Bernardo");
        usuario.setEmail("bernardo@email.com");

        Jogador jogador = new Jogador();
        jogador.setId(jogadorId);
        jogador.setNomeCompleto("Arrascaeta");

        // Inicializa lista com o jogador
        List<Jogador> observados = new ArrayList<>();
        observados.add(jogador);
        usuario.setJogadoresObservados(observados);

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(jogadorRepository.findById(jogadorId)).thenReturn(Optional.of(jogador));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Acao
        UsuarioResponseDTO resultado = usuarioService.removerJogadorObservado(usuarioId, jogadorId);

        // Verificacao
        assertNotNull(resultado);
        assertTrue(resultado.jogadoresObservados().isEmpty(), "A lista de jogadores observados deve estar vazia");

        verify(usuarioRepository, times(1)).save(usuario);
    }
}
