package com.credineyra.repository;

import com.credineyra.entity.PagoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PagoDetalleRepository
        extends JpaRepository<PagoDetalle, Long> {

    List<PagoDetalle> findByPago_IdPago(Long idPago);

    List<PagoDetalle> findByCuota_IdCuota(Long idCuota);

    List<PagoDetalle> findByCredito_IdCredito(Long idCredito);
}