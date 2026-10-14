package com.credineyra.repository;

import com.credineyra.entity.Cliente;
import com.credineyra.entity.enums.EstadoCliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository
        extends JpaRepository<Cliente, Long> {


    /*
     * Cargamos Persona junto con Cliente.
     *
     * Esto es importante porque tienes:
     *
     * spring.jpa.open-in-view = false
     *
     * y Cliente.persona está configurado como LAZY.
     */

    @Override
    @EntityGraph(attributePaths = "persona")
    List<Cliente> findAll();


    @Override
    @EntityGraph(attributePaths = "persona")
    Optional<Cliente> findById(
            Long idCliente
    );


    @EntityGraph(attributePaths = "persona")
    Optional<Cliente> findByCodigoCliente(
            String codigoCliente
    );


    @EntityGraph(attributePaths = "persona")
    Optional<Cliente> findByPersona_Dni(
            String dni
    );


    boolean existsByCodigoCliente(
            String codigoCliente
    );


    @EntityGraph(attributePaths = "persona")
    List<Cliente> findByEstado(
            EstadoCliente estado
    );

}