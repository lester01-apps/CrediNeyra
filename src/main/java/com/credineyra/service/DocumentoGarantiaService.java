package com.credineyra.service;

import com.credineyra.entity.Credito;
import com.credineyra.entity.Documento;
import com.credineyra.entity.Garantia;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.GarantiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class DocumentoGarantiaService {

    private static final long MAX_ARCHIVO =
            10L
                    * 1024L
                    * 1024L;


    private final CreditoRepository creditoRepository;

    private final GarantiaRepository garantiaRepository;

    private final DocumentoRepository documentoRepository;

    private final DocumentoStorageService storageService;


    public DocumentoGarantiaService(
            CreditoRepository creditoRepository,
            GarantiaRepository garantiaRepository,
            DocumentoRepository documentoRepository,
            DocumentoStorageService storageService
    ) {

        this.creditoRepository =
                creditoRepository;

        this.garantiaRepository =
                garantiaRepository;

        this.documentoRepository =
                documentoRepository;

        this.storageService =
                storageService;
    }


    /*
     * =========================================================
     * SUBIR EVIDENCIA DE GARANTÍA
     * =========================================================
     */

    @Transactional
    public Documento subir(
            Long idCredito,
            Long idGarantia,
            MultipartFile archivo,
            String observaciones
    ) {

        Credito credito =
                creditoRepository
                        .findById(
                                idCredito
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Crédito no encontrado."
                                        )
                        );


        Garantia garantia =
                garantiaRepository
                        .findById(
                                idGarantia
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Garantía no encontrada."
                                        )
                        );


        if (
                garantia.getCredito() == null
                        ||
                        !idCredito.equals(
                                garantia
                                        .getCredito()
                                        .getIdCredito()
                        )
        ) {

            throw new IllegalArgumentException(
                    "La garantía no pertenece al crédito."
            );
        }


        validarArchivo(
                archivo
        );


        byte[] contenido =
                leerArchivo(
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


        String nombreOriginal =
                sanitizarNombre(
                        archivo.getOriginalFilename()
                );


        if (nombreOriginal.isBlank()) {

            nombreOriginal =
                    "garantia"
                            + extension;
        }


        String momento =
                OffsetDateTime
                        .now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMdd-HHmmss-SSS"
                                )
                        );


        String ruta =
                "creditos/"
                        + credito.getCodigoCredito()
                        + "/garantias/"
                        + garantia.getIdGarantia()
                        + "/"
                        + momento
                        + "-"
                        + nombreOriginal;


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


        documento.setGarantia(
                garantia
        );


        documento.setTipoDocumento(
                TipoDocumento.DOCUMENTO_GARANTIA
        );


        documento.setCodigoDocumento(
                generarCodigoDocumento()
        );


        documento.setNombreArchivo(
                limitar(
                        nombreOriginal,
                        200
                )
        );


        documento.setRutaArchivo(
                limitar(
                        ruta,
                        500
                )
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
                limitar(
                        limpiar(
                                observaciones
                        ),
                        200
                )
        );


        return documentoRepository
                .save(
                        documento
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

            throw new IllegalArgumentException(
                    "Debes adjuntar el documento o foto de la garantía."
            );
        }


        if (
                archivo.getSize() <= 0
                        ||
                        archivo.getSize() > MAX_ARCHIVO
        ) {

            throw new IllegalArgumentException(
                    "El archivo de garantía debe pesar como máximo 10 MB."
            );
        }
    }


    private byte[] leerArchivo(
            MultipartFile archivo
    ) {

        try {

            return archivo.getBytes();

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "No se pudo leer el archivo de garantía.",
                    ex
            );
        }
    }


    /*
     * No confiamos únicamente en la extensión.
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
                        &&
                        bytes[4] == 0x0D
                        &&
                        bytes[5] == 0x0A
                        &&
                        bytes[6] == 0x1A
                        &&
                        bytes[7] == 0x0A
        ) {

            return "image/png";
        }


        /*
         * JPEG
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
                "Solo se permiten PDF, JPG, JPEG o PNG."
        );
    }


    private String extension(
            String contentType
    ) {

        return switch (
                contentType
                ) {

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


    private String generarCodigoDocumento() {

        return "GAR-"
                + UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase();
    }


    private String sanitizarNombre(
            String nombre
    ) {

        if (
                nombre == null
                        ||
                        nombre.isBlank()
        ) {

            return "";
        }


        return nombre
                .trim()
                .replaceAll(
                        "[^a-zA-Z0-9._-]",
                        "_"
                );
    }


    private String limpiar(
            String texto
    ) {

        if (texto == null) {
            return null;
        }


        String limpio =
                texto.trim();


        return limpio.isBlank()
                ? null
                : limpio;
    }


    private String limitar(
            String texto,
            int maximo
    ) {

        if (texto == null) {
            return null;
        }


        return texto.length() <= maximo
                ? texto
                : texto.substring(
                0,
                maximo
        );
    }
}