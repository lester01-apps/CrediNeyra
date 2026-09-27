package com.credineyra.repository;

import com.credineyra.entity.Pago;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.entity.enums.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository
        extends JpaRepository<Pago, Long> {

    Optional<Pago> findByIdempotencyKey(String idempotencyKey);

    List<Pago> findByCredito_IdCreditoOrderByFechaPagoDesc(
            Long idCredito
    );

    List<Pago> findByEstado(EstadoPago estado);

    boolean existsByMetodoPagoAndReferencia(
            MetodoPago metodoPago,
            String referencia
    );
}