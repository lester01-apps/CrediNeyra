package com.credineyra.repository;

import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByPersona_Dni(String dni);

    boolean existsByUsername(String username);

    List<Usuario> findByEstado(EstadoUsuario estado);
}