package com.credineyra.repository;

import com.credineyra.entity.Documento;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository
        extends JpaRepository<Documento, Long> {

    Optional<Documento> findByCodigoDocumento(
            String codigoDocumento
    );

    List<Documento> findByCliente_IdClienteOrderByFechaSubidaDesc(
            Long idCliente
    );

    List<Documento> findByCredito_IdCredito(
            Long idCredito
    );

    List<Documento> findByTipoDocumento(
            TipoDocumento tipoDocumento
    );

    List<Documento> findByEstado(
            EstadoDocumento estado
    );
}