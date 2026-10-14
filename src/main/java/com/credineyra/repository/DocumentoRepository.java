package com.credineyra.repository;

import com.credineyra.entity.Documento;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentoRepository
        extends JpaRepository<Documento, Long> {


    /*
     * =========================================================
     * CÓDIGO DOCUMENTO
     * =========================================================
     */

    Optional<Documento> findByCodigoDocumento(
            String codigoDocumento
    );


    boolean existsByCodigoDocumento(
            String codigoDocumento
    );


    /*
     * =========================================================
     * CLIENTE
     * =========================================================
     */

    List<Documento>
    findByCliente_IdClienteOrderByFechaSubidaDesc(
            Long idCliente
    );


    /*
     * =========================================================
     * CRÉDITO
     * =========================================================
     *
     * Se mantienen LOS DOS métodos.
     *
     * El primero lo usa DocumentoService antiguo.
     * El segundo lo usaremos en las pantallas nuevas.
     * =========================================================
     */

    List<Documento>
    findByCredito_IdCredito(
            Long idCredito
    );


    List<Documento>
    findByCredito_IdCreditoOrderByFechaSubidaDesc(
            Long idCredito
    );


    /*
     * =========================================================
     * GARANTÍA
     * =========================================================
     */

    List<Documento>
    findByGarantia_IdGarantiaOrderByFechaSubidaDesc(
            Long idGarantia
    );


    /*
     * =========================================================
     * TIPO
     * =========================================================
     */

    List<Documento>
    findByTipoDocumento(
            TipoDocumento tipoDocumento
    );


    /*
     * =========================================================
     * ESTADO
     * =========================================================
     */

    List<Documento>
    findByEstado(
            EstadoDocumento estado
    );


    /*
     * =========================================================
     * ÚLTIMO DOCUMENTO DE UN TIPO
     * =========================================================
     */

    Optional<Documento>
    findFirstByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
            Long idCredito,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );


    /*
     * =========================================================
     * DOCUMENTOS DE UN TIPO
     * =========================================================
     */

    List<Documento>
    findByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
            Long idCredito,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );


    /*
     * =========================================================
     * EXISTENCIA
     * =========================================================
     */

    boolean
    existsByCredito_IdCreditoAndTipoDocumentoAndEstado(
            Long idCredito,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );
}