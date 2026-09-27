package com.credineyra.repository;

import com.credineyra.entity.Credito;
import com.credineyra.entity.enums.EstadoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditoRepository
        extends JpaRepository<Credito, Long> {

    Optional<Credito> findByCodigoCredito(String codigoCredito);

    boolean existsByCodigoCredito(String codigoCredito);

    List<Credito> findByCliente_IdClienteOrderByFechaSolicitudDesc(
            Long idCliente
    );

    List<Credito> findByEstado(EstadoCredito estado);
}