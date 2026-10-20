package com.credineyra.repository;

import com.credineyra.entity.UsuarioRol;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRolRepository
        extends JpaRepository<UsuarioRol, UsuarioRol.UsuarioRolId> {

    @EntityGraph(attributePaths = "rol")
    List<UsuarioRol> findByUsuario_IdUsuario(Long idUsuario);

    List<UsuarioRol> findByRol_IdRol(Long idRol);
}
