package com.credineyra.repository;

import com.credineyra.entity.CondicionCredito;
import com.credineyra.entity.enums.TipoCalculoCredito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CondicionCreditoRepository
        extends JpaRepository<CondicionCredito, Long> {

    List<CondicionCredito> findByTipoCalculo(
            TipoCalculoCredito tipoCalculo
    );
}