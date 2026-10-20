package com.credineyra.service;

import com.credineyra.entity.Documento;
import com.credineyra.entity.Pago;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PagoComprobanteService {

    /*
     * =========================================================
     * MÁXIMO 10 MB
     * =========================================================
     */

    private static final long MAX_ARCHIVO =
            10L * 1024L * 1024L;


    private final PagoRepository pagoRepository;

    private final DocumentoRepository documentoRepository;

    private final DocumentoStorageService storageService;


    public PagoComprobanteService(
            PagoRepository pagoRepository,
            DocumentoRepository documentoRepository,
            DocumentoStorageService storageService
    ) {

        this.pagoRepository =
                pagoRepository;

        this.documentoRepository =
                documentoRepository;

        this.storageService =
                storageService;
    }


    /*
     * =========================================================
     * SUBIR COMPROBANTE
     * =========================================================
     */

    @Transactional
    public Documento subirComprobante(
            Long idPago,
            MultipartFile archivo
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
                documentoRepository
                        .existsByPago_IdPagoAndTipoDocumentoAndEstado(
                                idPago,
                                TipoDocumento.COMPROBANTE_PAGO,
                                EstadoDocumento.ACTIVO
                        )
        ) {

            throw new ReglaNegocioException(
                    "Este pago ya tiene un comprobante registrado"
            );
        }


        validarArchivo(
                archivo
        );


        byte[] contenido =
                leer(
                        archivo
                );


        String contentType =
                detectarTipo(
                        contenido
                );


        String extension =
                extension(
                        contentType
                );


        String fecha =
                OffsetDateTime
                        .now()
                        .format(
                                DateTimeFormatter
                                        .ofPattern(
                                                "yyyyMMdd-HHmmss-SSS"
                                        )
                        );


        String codigoCredito =
                pago
                        .getCredito()
                        .getCodigoCredito();


        String nombre =
                "PAGO-"
                        + idPago
                        + "-comprobante"
                        + extension;


        String ruta =
                "creditos/"
                        + codigoCredito
                        + "/pagos/"
                        + idPago
                        + "/comprobantes/"
                        + fecha
                        + "-"
                        + nombre;


        /*
         * El archivo queda PRIVADO en Supabase.
         */
        storageService.subir(
                contenido,
                ruta,
                contentType,
                false
        );


        Documento documento =
                new Documento();


        documento.setCliente(
                pago
                        .getCredito()
                        .getCliente()
        );


        documento.setCredito(
                pago.getCredito()
        );


        documento.setPago(
                pago
        );


        documento.setGarantia(
                null
        );


        documento.setTipoDocumento(
                TipoDocumento.COMPROBANTE_PAGO
        );


        documento.setCodigoDocumento(
                "COMP-PAG-"
                        + idPago
                        + "-"
                        + System.currentTimeMillis()
        );


        documento.setNombreArchivo(
                nombre
        );


        documento.setRutaArchivo(
                ruta
        );


        /*
         * El comprobante enviado por el cliente
         * puede volver a verlo el propio cliente.
         */
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
                "Comprobante presentado para validación del pago"
        );


        return documentoRepository
                .save(
                        documento
                );
    }


    /*
     * =========================================================
     * OBTENER COMPROBANTE
     * =========================================================
     */

    @Transactional(readOnly = true)
    public Documento obtenerComprobante(
            Long idPago
    ) {

        return documentoRepository
                .findFirstByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                        idPago,
                        TipoDocumento.COMPROBANTE_PAGO,
                        EstadoDocumento.ACTIVO
                )
                .orElseThrow(
                        () ->
                                new RecursoNoEncontradoException(
                                        "El pago no tiene comprobante"
                                )
                );
    }


    /*
     * =========================================================
     * URL TEMPORAL
     * =========================================================
     */

    @Transactional(readOnly = true)
    public String crearUrlTemporal(
            Long idPago
    ) {

        Documento documento =
                obtenerComprobante(
                        idPago
                );


        return storageService
                .crearUrlFirmada(
                        documento.getRutaArchivo(),
                        600
                );
    }


    /*
     * =========================================================
     * VALIDACIÓN
     * =========================================================
     */

    private void validarArchivo(
            MultipartFile archivo
    ) {

        if (
                archivo == null
                        ||
                        archivo.isEmpty()
        ) {

            throw new ReglaNegocioException(
                    "Debes adjuntar el comprobante del pago"
            );
        }


        if (
                archivo.getSize() <= 0
                        ||
                        archivo.getSize()
                                > MAX_ARCHIVO
        ) {

            throw new ReglaNegocioException(
                    "El comprobante debe pesar como máximo 10 MB"
            );
        }


        detectarTipo(
                leer(archivo)
        );
    }


    /*
     * =========================================================
     * LEER ARCHIVO
     * =========================================================
     */

    private byte[] leer(
            MultipartFile archivo
    ) {

        try {

            return archivo
                    .getBytes();

        } catch (IOException ex) {

            throw new ReglaNegocioException(
                    "No se pudo leer el comprobante"
            );
        }
    }


    /*
     * =========================================================
     * DETECTAR TIPO REAL
     * =========================================================
     */

    private String detectarTipo(
            byte[] bytes
    ) {

        /*
         * PDF
         */
        if (
                bytes.length >= 5
                        &&
                        bytes[0] == '%'
                        &&
                        bytes[1] == 'P'
                        &&
                        bytes[2] == 'D'
                        &&
                        bytes[3] == 'F'
                        &&
                        bytes[4] == '-'
        ) {

            return "application/pdf";
        }


        /*
         * PNG
         */
        if (
                bytes.length >= 8
                        &&
                        (bytes[0] & 0xFF) == 0x89
                        &&
                        bytes[1] == 0x50
                        &&
                        bytes[2] == 0x4E
                        &&
                        bytes[3] == 0x47
        ) {

            return "image/png";
        }


        /*
         * JPG / JPEG
         */
        if (
                bytes.length >= 3
                        &&
                        (bytes[0] & 0xFF) == 0xFF
                        &&
                        (bytes[1] & 0xFF) == 0xD8
                        &&
                        (bytes[2] & 0xFF) == 0xFF
        ) {

            return "image/jpeg";
        }


        throw new ReglaNegocioException(
                "El comprobante debe ser PDF, JPG, JPEG o PNG"
        );
    }


    /*
     * =========================================================
     * EXTENSIÓN
     * =========================================================
     */

    private String extension(
            String contentType
    ) {

        return switch (contentType) {

            case "application/pdf" ->
                    ".pdf";

            case "image/png" ->
                    ".png";

            case "image/jpeg" ->
                    ".jpg";

            default ->
                    "";
        };
    }
}