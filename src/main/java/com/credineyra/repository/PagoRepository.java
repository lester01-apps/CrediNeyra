package com.credineyra.repository;

import com.credineyra.entity.Pago;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.entity.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository
        extends JpaRepository<Pago, Long> {

    Optional<Pago> findByIdempotencyKey(
            String idempotencyKey
    );

    List<Pago>
    findAllByOrderByFechaPagoDesc();

    List<Pago>
    findByCredito_IdCreditoOrderByFechaPagoDesc(
            Long idCredito
    );

    List<Pago>
    findByCredito_Cliente_IdClienteOrderByFechaPagoDesc(
            Long idCliente
    );

    List<Pago>
    findByCredito_Cliente_Persona_IdPersonaOrderByFechaPagoDesc(
            Long idPersona
    );

    List<Pago> findByEstado(
            EstadoPago estado
    );

    List<Pago>
    findByEstadoOrderByFechaPagoAsc(
            EstadoPago estado
    );

    long countByEstado(
            EstadoPago estado
    );

    boolean existsByMetodoPagoAndReferencia(
            MetodoPago metodoPago,
            String referencia
    );

    List<Pago>
    findByEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
            EstadoPago estado,
            OffsetDateTime inicio,
            OffsetDateTime fin
    );
}