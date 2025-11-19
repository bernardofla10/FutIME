package com.futime.labprog.futimeapi.repository;

import com.futime.labprog.futimeapi.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    UserDetails findByEmail(String email);
}
