package com.credineyra.service;

import com.credineyra.entity.Credito;
import com.credineyra.entity.Documento;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.DocumentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ContratoFirmadoService {

    private static final long MAX_ARCHIVO =
            10L * 1024L * 1024L;


    private final CreditoRepository creditoRepository;

    private final DocumentoRepository documentoRepository;

    private final DocumentoStorageService storageService;


    public ContratoFirmadoService(
            CreditoRepository creditoRepository,
            DocumentoRepository documentoRepository,
            DocumentoStorageService storageService
    ) {

        this.creditoRepository =
                creditoRepository;

        this.documentoRepository =
                documentoRepository;

        this.storageService =
                storageService;
    }


    @Transactional
    public Documento subir(
            Long idCredito,
            MultipartFile archivo
    ) {

        Credito credito =
                creditoRepository
                        .findById(idCredito)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Crédito no encontrado."
                                        )
                        );


        boolean yaExiste =
                documentoRepository
                        .existsByCredito_IdCreditoAndTipoDocumentoAndEstado(
                                idCredito,
                                TipoDocumento.CONTRATO_FIRMADO,
                                EstadoDocumento.ACTIVO
                        );


        if (yaExiste) {

            throw new IllegalStateException(
                    "Este crédito ya tiene un contrato firmado registrado."
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
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd-HHmmss-SSS"
                                )
                        );


        String nombreArchivo =
                credito.getCodigoCredito()
                        + "-contrato-firmado"
                        + extension;


        String ruta =
                "creditos/"
                        + credito.getCodigoCredito()
                        + "/contratos-firmados/"
                        + fecha
                        + "-"
                        + nombreArchivo;


        storageService.subir(
                contenido,
                ruta,
                contentType,
                false
        );


        Documento documento =
                new Documento();


        documento.setCliente(
                credito.getCliente()
        );


        documento.setCredito(
                credito
        );


        documento.setPago(
                null
        );


        documento.setGarantia(
                null
        );


        documento.setTipoDocumento(
                TipoDocumento.CONTRATO_FIRMADO
        );


        documento.setCodigoDocumento(
                "CTR-FIR-"
                        + idCredito
                        + "-"
                        + System.currentTimeMillis()
        );


        documento.setNombreArchivo(
                nombreArchivo
        );


        documento.setRutaArchivo(
                ruta
        );


        documento.setVisibleCliente(
                false
        );


        documento.setEstado(
                EstadoDocumento.ACTIVO
        );


        documento.setFechaSubida(
                OffsetDateTime.now()
        );


        documento.setObservaciones(
                "Contrato firmado por las partes"
        );


        return documentoRepository
                .save(
                        documento
                );
    }


    private void validarArchivo(
            MultipartFile archivo
    ) {

        if (
                archivo == null
                        ||
                        archivo.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Debes adjuntar el contrato firmado."
            );
        }


        if (
                archivo.getSize() <= 0
                        ||
                        archivo.getSize() > MAX_ARCHIVO
        ) {

            throw new IllegalArgumentException(
                    "El contrato firmado debe pesar como máximo 10 MB."
            );
        }


        detectarTipo(
                leer(archivo)
        );
    }


    private byte[] leer(
            MultipartFile archivo
    ) {

        try {

            return archivo.getBytes();

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No se pudo leer el contrato firmado.",
                    ex
            );
        }
    }


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


        throw new IllegalArgumentException(
                "El contrato firmado debe ser PDF, JPG, JPEG o PNG."
        );
    }


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