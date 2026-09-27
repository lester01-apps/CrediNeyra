package com.credineyra.repository;

import com.credineyra.entity.Garantia;
import com.credineyra.entity.enums.EstadoGarantia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarantiaRepository
        extends JpaRepository<Garantia, Long> {

    List<Garantia> findByCredito_IdCredito(Long idCredito);

    List<Garantia> findByEstado(EstadoGarantia estado);
}