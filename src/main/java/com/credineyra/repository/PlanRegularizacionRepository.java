package com.credineyra.repository;

import com.credineyra.entity.PlanRegularizacion;
import com.credineyra.entity.enums.EstadoPlanRegularizacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRegularizacionRepository
        extends JpaRepository<PlanRegularizacion, Long> {

    List<PlanRegularizacion>
    findByCredito_IdCreditoOrderByFechaCreacionDesc(
            Long idCredito
    );

    List<PlanRegularizacion> findByEstado(
            EstadoPlanRegularizacion estado
    );
}