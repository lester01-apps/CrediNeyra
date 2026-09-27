package com.credineyra.repository;

import com.credineyra.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByModuloAndNombre(
            String modulo,
            String nombre
    );

    List<Permiso> findByModulo(String modulo);
}