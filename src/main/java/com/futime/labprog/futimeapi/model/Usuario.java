package com.futime.labprog.futimeapi.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clube_favorito_id")
    private Clube clubeFavorito;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_jogadores_observados", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "jogador_id"))
    private List<Jogador> jogadoresObservados;

    public Usuario() {
    }

    public Usuario(String email, String senha, String nome) {
        this.email = email;
        this.senha = senha;
        this.nome = nome;
    }

    // Getters e Setters básicos
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Clube getClubeFavorito() {
        return clubeFavorito;
    }

    public void setClubeFavorito(Clube clubeFavorito) {
        this.clubeFavorito = clubeFavorito;
    }

    public List<Jogador> getJogadoresObservados() {
        return jogadoresObservados;
    }

    public void setJogadoresObservados(List<Jogador> jogadoresObservados) {
        this.jogadoresObservados = jogadoresObservados;
    }

    // Implementação de UserDetails

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por enquanto, não temos roles específicas, retornamos lista vazia.
        // Poderíamos retornar List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return List.of();
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
