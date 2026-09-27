package com.credineyra.repository;

import com.credineyra.entity.Cuota;
import com.credineyra.entity.enums.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CuotaRepository
        extends JpaRepository<Cuota, Long> {

    List<Cuota> findByCredito_IdCreditoOrderByNumeroCuotaAsc(
            Long idCredito
    );

    Optional<Cuota> findByCredito_IdCreditoAndNumeroCuota(
            Long idCredito,
            Integer numeroCuota
    );

    List<Cuota> findByEstadoAndFechaVencimientoLessThanEqual(
            EstadoCuota estado,
            LocalDate fecha
    );

    List<Cuota> findByFechaVencimiento(LocalDate fechaVencimiento);
}