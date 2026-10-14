package com.credineyra.service;

import com.credineyra.dto.ContratoCreditoDto;
import com.credineyra.dto.DocumentoResumenDto;
import com.credineyra.entity.*;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.GarantiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class DocumentoCreditoService {

    private static final long MAX_ARCHIVO =
            10L * 1024L * 1024L;

    private static final DateTimeFormatter CODIGO_FECHA =
            DateTimeFormatter.ofPattern(
                    "yyyyMMddHHmmss"
            );

    private final DocumentoRepository documentoRepository;
    private final CreditoRepository creditoRepository;
    private final GarantiaRepository garantiaRepository;

    private final ContratoCreditoService contratoCreditoService;
    private final ContratoPdfService contratoPdfService;
    private final DocumentoStorageService storageService;

    public DocumentoCreditoService(
            DocumentoRepository documentoRepository,
            CreditoRepository creditoRepository,
            GarantiaRepository garantiaRepository,
            ContratoCreditoService contratoCreditoService,
            ContratoPdfService contratoPdfService,
            DocumentoStorageService storageService
    ) {
        this.documentoRepository =
                documentoRepository;

        this.creditoRepository =
                creditoRepository;

        this.garantiaRepository =
                garantiaRepository;

        this.contratoCreditoService =
                contratoCreditoService;

        this.contratoPdfService =
                contratoPdfService;

        this.storageService =
                storageService;
    }

    public Documento generarContratoSiFalta(
            Long idCredito
    ) {

        Documento existente =
                documentoRepository
                        .findFirstByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                idCredito,
                                TipoDocumento.CONTRATO_GENERADO,
                                EstadoDocumento.ACTIVO
                        )
                        .orElse(null);

        if (existente != null) {
            return existente;
        }

        Credito credito =
                buscarCredito(
                        idCredito
                );

        ContratoCreditoDto contrato =
                contratoCreditoService
                        .construir(
                                idCredito
                        );

        byte[] pdf =
                contratoPdfService
                        .generar(
                                contrato
                        );

        String nombre =
                credito.getCodigoCredito()
                        + "-contrato-generado.pdf";

        String ruta =
                "creditos/"
                        + credito.getCodigoCredito()
                        + "/contratos/"
                        + nombre;

        storageService.subir(
                pdf,
                ruta,
                "application/pdf",
                true
        );

        Documento documento =
                new Documento();

        documento.setCliente(
                credito.getCliente()
        );

        documento.setCredito(
                credito
        );

        documento.setTipoDocumento(
                TipoDocumento.CONTRATO_GENERADO
        );

        documento.setCodigoDocumento(
                "CTR-GEN-"
                        + credito.getIdCredito()
        );

        documento.setNombreArchivo(
                nombre
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

        documento.setObservaciones(
                "Contrato y cronograma generado automáticamente por crediNeyra"
        );

        return documentoRepository.save(
                documento
        );
    }

    public Documento subirContratoFirmado(
            Long idCredito,
            MultipartFile archivo
    ) {

        Credito credito =
                buscarCredito(
                        idCredito
                );

        validarArchivo(
                archivo,
                true
        );

        byte[] contenido =
                bytes(
                        archivo
                );

        if (!esPdf(contenido)) {

            throw new ReglaNegocioException(
                    "El contrato firmado debe ser un PDF válido"
            );
        }

        String marca =
                OffsetDateTime.now()
                        .format(
                                CODIGO_FECHA
                        );

        String nombre =
                credito.getCodigoCredito()
                        + "-contrato-firmado-"
                        + marca
                        + ".pdf";

        String ruta =
                "creditos/"
                        + credito.getCodigoCredito()
                        + "/contratos-firmados/"
                        + nombre;

        storageService.subir(
                contenido,
                ruta,
                "application/pdf",
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

        documento.setTipoDocumento(
                TipoDocumento.CONTRATO_FIRMADO
        );

        documento.setCodigoDocumento(
                "CTR-FIR-"
                        + credito.getIdCredito()
                        + "-"
                        + marca
        );

        documento.setNombreArchivo(
                nombre
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

        documento.setObservaciones(
                "Contrato firmado y digitalizado"
        );

        return documentoRepository.save(
                documento
        );
    }

    public Documento subirDocumentoGarantia(
            Long idCredito,
            Long idGarantia,
            MultipartFile archivo
    ) {

        Credito credito =
                buscarCredito(
                        idCredito
                );

        Garantia garantia =
                garantiaRepository
                        .findById(
                                idGarantia
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Garantía no encontrada"
                                        )
                        );

        if (
                !garantia.getCredito()
                        .getIdCredito()
                        .equals(
                                credito.getIdCredito()
                        )
        ) {

            throw new ReglaNegocioException(
                    "La garantía no pertenece al crédito"
            );
        }

        validarArchivo(
                archivo,
                false
        );

        byte[] contenido =
                bytes(
                        archivo
                );

        String extension =
                detectarExtension(
                        contenido
                );

        String contentType =
                detectarContentType(
                        extension
                );

        String marca =
                OffsetDateTime.now()
                        .format(
                                CODIGO_FECHA
                        );

        String nombre =
                "garantia-"
                        + garantia.getIdGarantia()
                        + "-"
                        + marca
                        + "."
                        + extension;

        String ruta =
                "creditos/"
                        + credito.getCodigoCredito()
                        + "/garantias/"
                        + garantia.getIdGarantia()
                        + "/"
                        + nombre;

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
                "GAR-"
                        + garantia.getIdGarantia()
                        + "-"
                        + marca
        );

        documento.setNombreArchivo(
                nombre
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

        documento.setObservaciones(
                "Documento asociado a la garantía"
        );

        return documentoRepository.save(
                documento
        );
    }

    @Transactional(readOnly = true)
    public List<DocumentoResumenDto> listarCredito(
            Long idCredito
    ) {

        buscarCredito(
                idCredito
        );

        return documentoRepository
                .findByCredito_IdCreditoOrderByFechaSubidaDesc(
                        idCredito
                )
                .stream()
                .map(
                        this::convertirResumen
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public String crearUrlTemporal(
            Long idDocumento
    ) {

        Documento documento =
                documentoRepository
                        .findById(
                                idDocumento
                        )
                        .orElseThrow(
                                () ->
                                        new RecursoNoEncontradoException(
                                                "Documento no encontrado"
                                        )
                        );

        if (
                documento.getEstado()
                        != EstadoDocumento.ACTIVO
        ) {

            throw new ReglaNegocioException(
                    "El documento está anulado"
            );
        }

        /*
         * URL válida durante 10 minutos.
         */
        return storageService
                .crearUrlFirmada(
                        documento.getRutaArchivo(),
                        600
                );
    }

    private Credito buscarCredito(
            Long idCredito
    ) {

        return creditoRepository
                .findById(
                        idCredito
                )
                .orElseThrow(
                        () ->
                                new RecursoNoEncontradoException(
                                        "Crédito no encontrado"
                                )
                );
    }

    private DocumentoResumenDto convertirResumen(
            Documento documento
    ) {

        return new DocumentoResumenDto(
                documento.getIdDocumento(),
                documento.getCodigoDocumento(),
                documento.getTipoDocumento(),
                documento.getNombreArchivo(),
                documento.getEstado(),
                documento.getFechaSubida(),
                documento.getVisibleCliente(),
                documento.getObservaciones()
        );
    }

    private void validarArchivo(
            MultipartFile archivo,
            boolean soloPdf
    ) {

        if (
                archivo == null
                        ||
                        archivo.isEmpty()
        ) {

            throw new ReglaNegocioException(
                    "Debe seleccionar un archivo"
            );
        }

        if (
                archivo.getSize()
                        > MAX_ARCHIVO
        ) {

            throw new ReglaNegocioException(
                    "El archivo no puede superar 10 MB"
            );
        }

        if (
                soloPdf
                        &&
                        archivo.getOriginalFilename() != null
                        &&
                        !archivo.getOriginalFilename()
                                .toLowerCase()
                                .endsWith(".pdf")
        ) {

            throw new ReglaNegocioException(
                    "Solo se permite PDF"
            );
        }
    }

    private byte[] bytes(
            MultipartFile archivo
    ) {

        try {

            return archivo.getBytes();

        } catch (IOException e) {

            throw new ReglaNegocioException(
                    "No se pudo leer el archivo"
            );
        }
    }

    private String detectarExtension(
            byte[] contenido
    ) {

        if (esPdf(contenido)) {
            return "pdf";
        }

        if (esPng(contenido)) {
            return "png";
        }

        if (esJpeg(contenido)) {
            return "jpg";
        }

        throw new ReglaNegocioException(
                "Solo se permiten archivos PDF, JPG, JPEG o PNG"
        );
    }

    private String detectarContentType(
            String extension
    ) {

        return switch (extension) {

            case "pdf" ->
                    "application/pdf";

            case "png" ->
                    "image/png";

            case "jpg" ->
                    "image/jpeg";

            default ->
                    "application/octet-stream";
        };
    }

    private boolean esPdf(
            byte[] datos
    ) {

        return datos.length >= 5
                &&
                datos[0] == '%'
                &&
                datos[1] == 'P'
                &&
                datos[2] == 'D'
                &&
                datos[3] == 'F'
                &&
                datos[4] == '-';
    }

    private boolean esPng(
            byte[] datos
    ) {

        return datos.length >= 8
                &&
                (datos[0] & 0xFF) == 0x89
                &&
                datos[1] == 0x50
                &&
                datos[2] == 0x4E
                &&
                datos[3] == 0x47
                &&
                datos[4] == 0x0D
                &&
                datos[5] == 0x0A
                &&
                datos[6] == 0x1A
                &&
                datos[7] == 0x0A;
    }

    private boolean esJpeg(
            byte[] datos
    ) {

        return datos.length >= 3
                &&
                (datos[0] & 0xFF) == 0xFF
                &&
                (datos[1] & 0xFF) == 0xD8
                &&
                (datos[2] & 0xFF) == 0xFF;
    }
}