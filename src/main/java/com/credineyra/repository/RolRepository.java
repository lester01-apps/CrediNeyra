package com.credineyra.repository;

import com.credineyra.entity.Rol;
import com.credineyra.entity.enums.EstadoRol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);

    List<Rol> findByEstado(EstadoRol estado);
}