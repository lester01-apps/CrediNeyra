package com.credineyra.repository;

import com.credineyra.entity.Garantia;
import com.credineyra.entity.enums.EstadoGarantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GarantiaRepository
        extends JpaRepository<Garantia, Long> {

    /*
     * Método anterior usado por GarantiaService.
     */
    List<Garantia> findByCredito_IdCredito(
            Long idCredito
    );

    /*
     * Método anterior usado por GarantiaService.
     */
    List<Garantia> findByEstado(
            EstadoGarantia estado
    );

    /*
     * Nuevos métodos para documentos y detalle.
     */
    List<Garantia>
    findByCredito_IdCreditoOrderByFechaRegistroDesc(
            Long idCredito
    );

    Optional<Garantia>
    findFirstByCredito_IdCreditoOrderByFechaRegistroDesc(
            Long idCredito
    );
}