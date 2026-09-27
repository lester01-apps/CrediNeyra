package com.credineyra.repository;

import com.credineyra.entity.Tasa;
import com.credineyra.entity.enums.EstadoTasa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TasaRepository extends JpaRepository<Tasa, Long> {

    Optional<Tasa> findByCodigoAndVersion(
            String codigo,
            Integer version
    );

    List<Tasa> findByCodigoOrderByVersionDesc(String codigo);

    List<Tasa> findByEstado(EstadoTasa estado);
}