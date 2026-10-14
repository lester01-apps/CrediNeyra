package com.credineyra.repository;

import com.credineyra.entity.Cuota;
import com.credineyra.entity.enums.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuotaRepository
        extends JpaRepository<Cuota, Long> {


    /* =========================================================
       BUSCAR CUOTA POR CREDITO Y NUMERO
       ========================================================= */

    Optional<Cuota>
    findByCredito_IdCreditoAndNumeroCuota(
            Long idCredito,
            Integer numeroCuota
    );


    /* =========================================================
       LISTAR CUOTAS DE UN CREDITO
       ========================================================= */

    List<Cuota>
    findByCredito_IdCreditoOrderByNumeroCuotaAsc(
            Long idCredito
    );


    /* =========================================================
       LISTAR POR FECHA DE VENCIMIENTO
       ========================================================= */

    List<Cuota>
    findByCredito_IdCreditoOrderByFechaVencimientoAsc(
            Long idCredito
    );


    /* =========================================================
       CUOTAS VENCIDAS SEGUN ESTADO Y FECHA
       ========================================================= */

    List<Cuota>
    findByEstadoAndFechaVencimientoLessThanEqual(
            EstadoCuota estado,
            LocalDate fechaVencimiento
    );


    /* =========================================================
       CUOTAS DE UNA FECHA
       ========================================================= */

    List<Cuota>
    findByFechaVencimiento(
            LocalDate fechaVencimiento
    );


    /* =========================================================
       TODAS LAS CUOTAS DE UN CLIENTE
       ========================================================= */

    List<Cuota>
    findByCredito_Cliente_IdClienteOrderByFechaVencimientoAsc(
            Long idCliente
    );
}