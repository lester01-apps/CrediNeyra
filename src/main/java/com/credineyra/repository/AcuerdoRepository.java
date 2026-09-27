package com.credineyra.repository;

import com.credineyra.entity.Acuerdo;
import com.credineyra.entity.enums.EstadoAcuerdo;
import com.credineyra.entity.enums.TipoAcuerdo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcuerdoRepository
        extends JpaRepository<Acuerdo, Long> {

    List<Acuerdo> findByCredito_IdCreditoOrderByFechaCreacionDesc(
            Long idCredito
    );

    List<Acuerdo> findByEstado(
            EstadoAcuerdo estado
    );

    List<Acuerdo> findByTipoAcuerdo(
            TipoAcuerdo tipoAcuerdo
    );
}