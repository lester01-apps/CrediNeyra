package com.credineyra.repository;

import com.credineyra.entity.PlanRegularizacionDetalle;
import com.credineyra.entity.enums.EstadoPlanDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanRegularizacionDetalleRepository
        extends JpaRepository<PlanRegularizacionDetalle, Long> {

    List<PlanRegularizacionDetalle> findByPlan_IdPlan(
            Long idPlan
    );

    List<PlanRegularizacionDetalle> findByCuota_IdCuota(
            Long idCuota
    );

    List<PlanRegularizacionDetalle> findByEstado(
            EstadoPlanDetalle estado
    );
}