package com.credineyra.service;

import com.credineyra.dto.PagoDetalleVistaDto;
import com.credineyra.entity.Documento;
import com.credineyra.entity.Pago;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ConstanciaPagoService {

    private final PagoRepository pagoRepository;

    private final DocumentoRepository documentoRepository;

    private final PagoDetalleVistaService detalleVistaService;

    private final ConstanciaPagoPdfService pdfService;

    private final DocumentoStorageService storageService;

    public ConstanciaPagoService(
            PagoRepository pagoRepository,
            DocumentoRepository documentoRepository,
            PagoDetalleVistaService detalleVistaService,
            ConstanciaPagoPdfService pdfService,
            DocumentoStorageService storageService
    ) {

        this.pagoRepository =
                pagoRepository;

        this.documentoRepository =
                documentoRepository;

        this.detalleVistaService =
                detalleVistaService;

        this.pdfService =
                pdfService;

        this.storageService =
                storageService;
    }

    @Transactional
    public Documento generarSiFalta(
            Long idPago
    ) {

        Pago pago =
                pagoRepository
                        .findById(idPago)
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Pago no encontrado"
                                        )
                        );

        if (
                pago.getEstado()
                        != EstadoPago.APROBADO
        ) {

            throw new ReglaNegocioException(
                    "La constancia solo se genera para pagos aprobados"
            );
        }

        var existente =
                documentoRepository
                        .findFirstByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                idPago,
                                TipoDocumento.CONSTANCIA_PAGO,
                                EstadoDocumento.ACTIVO
                        );

        if (existente.isPresent()) {

            return existente.get();
        }

        PagoDetalleVistaDto dto =
                detalleVistaService
                        .obtener(idPago);

        byte[] pdf =
                pdfService
                        .generar(dto);

        String nombre =
                dto.codigoPago()
                        + "-constancia.pdf";

        String ruta =
                "creditos/"
                        + dto.codigoCredito()
                        + "/pagos/"
                        + idPago
                        + "/constancias/"
                        + nombre;

        storageService.subir(
                pdf,
                ruta,
                "application/pdf",
                false
        );

        Documento documento =
                new Documento();

        documento.setCliente(
                pago.getCredito()
                        .getCliente()
        );

        documento.setCredito(
                pago.getCredito()
        );

        documento.setPago(
                pago
        );

        documento.setTipoDocumento(
                TipoDocumento.CONSTANCIA_PAGO
        );

        documento.setCodigoDocumento(
                "CONST-PAG-"
                        + idPago
        );

        documento.setNombreArchivo(
                nombre
        );

        documento.setRutaArchivo(
                ruta
        );

        documento.setVisibleCliente(
                true
        );

        documento.setEstado(
                EstadoDocumento.ACTIVO
        );

        documento.setFechaSubida(
                OffsetDateTime.now()
        );

        documento.setObservaciones(
                "Constancia interna del pago aprobado"
        );

        return documentoRepository
                .save(documento);
    }
}