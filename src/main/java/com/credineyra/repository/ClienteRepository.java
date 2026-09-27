package com.credineyra.repository;

import com.credineyra.entity.Cliente;
import com.credineyra.entity.enums.EstadoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCodigoCliente(String codigoCliente);

    Optional<Cliente> findByPersona_Dni(String dni);

    boolean existsByCodigoCliente(String codigoCliente);

    List<Cliente> findByEstado(EstadoCliente estado);
}