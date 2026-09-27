package com.credineyra.repository;

import com.credineyra.entity.TraspasoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TraspasoClienteRepository
        extends JpaRepository<TraspasoCliente, Long> {

    Optional<TraspasoCliente> findByCliente_IdCliente(
            Long idCliente
    );

    boolean existsByCliente_IdCliente(
            Long idCliente
    );
}