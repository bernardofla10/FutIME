package com.futime.labprog.futimeapi.service;

import com.futime.labprog.futimeapi.dto.ClubeResponseDTO;
import com.futime.labprog.futimeapi.dto.EstadioResponseDTO;
import com.futime.labprog.futimeapi.dto.JogadorResponseDTO;
import com.futime.labprog.futimeapi.dto.RegisterDTO;
import com.futime.labprog.futimeapi.dto.UsuarioResponseDTO;
import com.futime.labprog.futimeapi.model.Clube;
import com.futime.labprog.futimeapi.model.Estadio;
import com.futime.labprog.futimeapi.model.Jogador;
import com.futime.labprog.futimeapi.model.Usuario;
import com.futime.labprog.futimeapi.repository.ClubeRepository;
import com.futime.labprog.futimeapi.repository.JogadorRepository;
import com.futime.labprog.futimeapi.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClubeRepository clubeRepository;
    private final JogadorRepository jogadorRepository;
    private final JogadorService jogadorService; // Injetado
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, ClubeRepository clubeRepository,
            JogadorRepository jogadorRepository, JogadorService jogadorService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clubeRepository = clubeRepository;
        this.jogadorRepository = jogadorRepository;
        this.jogadorService = jogadorService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UsuarioResponseDTO registerUser(RegisterDTO registerDTO) {
        // TODO: Verificar se email já existe
        String senhaCriptografada = passwordEncoder.encode(registerDTO.senha());
        Usuario novoUsuario = new Usuario(registerDTO.email(), senhaCriptografada, registerDTO.nome());
        Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);
        return toResponseDTO(usuarioSalvo);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO definirClubeFavorito(Integer usuarioId, Integer clubeId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioId));
        Clube clube = clubeRepository.findById(clubeId)
                .orElseThrow(() -> new EntityNotFoundException("Clube não encontrado com ID: " + clubeId));

        usuario.setClubeFavorito(clube);
        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return toResponseDTO(usuarioSalvo);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO adicionarJogadorObservado(Integer usuarioId, Integer jogadorId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioId));
        Jogador jogador = jogadorRepository.findById(jogadorId)
                .orElseThrow(() -> new EntityNotFoundException("Jogador não encontrado com ID: " + jogadorId));

        if (usuario.getJogadoresObservados() == null) {
            usuario.setJogadoresObservados(new ArrayList<>());
        }

        // Evita duplicatas
        if (!usuario.getJogadoresObservados().contains(jogador)) {
            usuario.getJogadoresObservados().add(jogador);
        }

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return toResponseDTO(usuarioSalvo);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPerfil(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com ID: " + usuarioId));
        return toResponseDTO(usuario);
    }

    // Helpers de conversão

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        ClubeResponseDTO clubeDTO = null;
        if (usuario.getClubeFavorito() != null) {
            clubeDTO = toClubeDTO(usuario.getClubeFavorito());
        }

        List<JogadorResponseDTO> jogadoresDTO = new ArrayList<>();
        if (usuario.getJogadoresObservados() != null) {
            jogadoresDTO = usuario.getJogadoresObservados().stream()
                    .map(this::toJogadorDTO)
                    .collect(Collectors.toList());
        }

        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                clubeDTO,
                jogadoresDTO);
    }

    private ClubeResponseDTO toClubeDTO(Clube clube) {
        EstadioResponseDTO estadioDTO = null;
        if (clube.getEstadio() != null) {
            Estadio estadio = clube.getEstadio();
            estadioDTO = new EstadioResponseDTO(estadio.getId(), estadio.getNome(), estadio.getCidade(),
                    estadio.getPais());
        }
        return new ClubeResponseDTO(
                clube.getId(),
                clube.getNome(),
                clube.getSigla(),
                clube.getCidade(),
                clube.getPais(),
                estadioDTO);
    }

    private JogadorResponseDTO toJogadorDTO(Jogador jogador) {
        // Delega a conversão completa (com estatísticas) para o JogadorService
        return jogadorService.toResponseDTO(jogador);
    }
}
