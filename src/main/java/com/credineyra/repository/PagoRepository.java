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


    List<Pago> findByCredito_IdCreditoOrderByFechaPagoDesc(
            Long idCredito
    );


    List<Pago> findByEstado(
            EstadoPago estado
    );


    boolean existsByMetodoPagoAndReferencia(
            MetodoPago metodoPago,
            String referencia
    );


    /*
     * Dashboard.
     *
     * Obtiene pagos de un estado específico
     * dentro de un período.
     *
     * inicio incluido.
     * fin excluido.
     */
    List<Pago> findByEstadoAndFechaPagoGreaterThanEqualAndFechaPagoLessThan(
            EstadoPago estado,
            OffsetDateTime inicio,
            OffsetDateTime fin
    );

}