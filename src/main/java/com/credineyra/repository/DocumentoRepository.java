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
     * CÓDIGO
     * =========================================================
     */

    Optional<Documento>
    findByCodigoDocumento(
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

    Optional<Documento>
    findFirstByCliente_IdClienteAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
            Long idCliente,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );


    /*
     * =========================================================
     * CRÉDITO
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
     * PAGO
     * =========================================================
     */

    List<Documento>
    findByPago_IdPagoOrderByFechaSubidaDesc(
            Long idPago
    );


    List<Documento>
    findByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
            Long idPago,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );


    Optional<Documento>
    findFirstByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
            Long idPago,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );


    boolean
    existsByPago_IdPagoAndTipoDocumentoAndEstado(
            Long idPago,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
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
     * ÚLTIMO DOCUMENTO DE CRÉDITO POR TIPO
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
     * DOCUMENTOS DE CRÉDITO POR TIPO
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
     * EXISTENCIA DOCUMENTO DE CRÉDITO
     * =========================================================
     */

    boolean
    existsByCredito_IdCreditoAndTipoDocumentoAndEstado(
            Long idCredito,
            TipoDocumento tipoDocumento,
            EstadoDocumento estado
    );
}
