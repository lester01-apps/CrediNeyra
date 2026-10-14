package com.credineyra.service;

import com.credineyra.dto.CreditoRegistroDto;
import com.credineyra.dto.CreditoRegistroResultadoDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Documento;
import com.credineyra.entity.Garantia;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.GarantiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
public class CreditoRegistroOrquestadorService {

    private static final BigDecimal LIMITE_GARANTIA =
            new BigDecimal("500.00");


    private final CreditoService creditoService;

    private final CreditoCodigoService codigoService;

    private final GarantiaRepository garantiaRepository;

    private final DocumentoRepository documentoRepository;

    private final DocumentoGarantiaService documentoGarantiaService;

    private final DocumentoCreditoService documentoCreditoService;


    public CreditoRegistroOrquestadorService(
            CreditoService creditoService,
            CreditoCodigoService codigoService,
            GarantiaRepository garantiaRepository,
            DocumentoRepository documentoRepository,
            DocumentoGarantiaService documentoGarantiaService,
            DocumentoCreditoService documentoCreditoService
    ) {

        this.creditoService =
                creditoService;

        this.codigoService =
                codigoService;

        this.garantiaRepository =
                garantiaRepository;

        this.documentoRepository =
                documentoRepository;

        this.documentoGarantiaService =
                documentoGarantiaService;

        this.documentoCreditoService =
                documentoCreditoService;
    }


    /*
     * =========================================================
     * REGISTRO COMPLETO
     * =========================================================
     *
     * 1. Validar
     * 2. Generar código
     * 3. Crear crédito
     * 4. Guardar evidencia de garantía
     * 5. Generar contrato PDF
     * 6. Devolver IDs para pantalla de éxito
     *
     * =========================================================
     */

    @Transactional
    public synchronized CreditoRegistroResultadoDto registrar(
            CreditoRegistroDto dto,
            MultipartFile archivoGarantia,
            Usuario usuario
    ) {

        validar(
                dto,
                archivoGarantia
        );


        /*
         * El backend genera el código.
         * No confiamos en lo que venga del navegador.
         */
        dto.setCodigoCredito(
                codigoService
                        .generarSiguienteCodigo()
        );


        Credito credito =
                creditoService
                        .registrarCompleto(
                                dto,
                                usuario
                        );


        boolean hayGarantia =
                requiereGarantia(dto)
                        ||
                        hayDatosGarantia(
                                dto,
                                archivoGarantia
                        );


        /*
         * =====================================================
         * DOCUMENTO / FOTO DE GARANTÍA
         * =====================================================
         */

        if (hayGarantia) {

            Garantia garantia =
                    garantiaRepository
                            .findFirstByCredito_IdCreditoOrderByFechaRegistroDesc(
                                    credito.getIdCredito()
                            )
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "No se encontró la garantía recién registrada."
                                            )
                            );


            documentoGarantiaService
                    .subir(
                            credito.getIdCredito(),
                            garantia.getIdGarantia(),
                            archivoGarantia,
                            dto.getObservacionesGarantia()
                    );
        }


        /*
         * =====================================================
         * CONTRATO PDF AUTOMÁTICO
         * =====================================================
         */

        documentoCreditoService
                .generarContratoSiFalta(
                        credito.getIdCredito()
                );


        Documento contrato =
                documentoRepository
                        .findFirstByCredito_IdCreditoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                credito.getIdCredito(),
                                TipoDocumento.CONTRATO_GENERADO,
                                EstadoDocumento.ACTIVO
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "El crédito se calculó, pero no se encontró el contrato PDF generado."
                                        )
                        );


        return new CreditoRegistroResultadoDto(
                credito.getIdCredito(),
                credito.getCodigoCredito(),
                contrato.getIdDocumento()
        );
    }


    /*
     * =========================================================
     * VALIDACIÓN
     * =========================================================
     */

    private void validar(
            CreditoRegistroDto dto,
            MultipartFile archivo
    ) {

        if (dto == null) {

            throw new IllegalArgumentException(
                    "Los datos del crédito son obligatorios."
            );
        }


        if (dto.getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "Debes seleccionar un cliente."
            );
        }


        if (
                dto.getCapitalOriginal() == null
                        ||
                        dto.getCapitalOriginal()
                                .compareTo(
                                        BigDecimal.ZERO
                                ) <= 0
        ) {

            throw new IllegalArgumentException(
                    "El capital es obligatorio."
            );
        }


        if (dto.getTasaInteres() == null) {

            throw new IllegalArgumentException(
                    "La tasa de interés es obligatoria."
            );
        }


        if (dto.getTipoCalculo() == null) {

            throw new IllegalArgumentException(
                    "Selecciona el tipo de interés."
            );
        }


        if (dto.getPeriodicidadTasa() == null) {

            throw new IllegalArgumentException(
                    "Selecciona la periodicidad del interés."
            );
        }


        if (dto.getFrecuenciaPago() == null) {

            throw new IllegalArgumentException(
                    "Selecciona la frecuencia de pago."
            );
        }


        if (
                dto.getPlazoMeses() == null
                        ||
                        dto.getPlazoMeses() <= 0
        ) {

            throw new IllegalArgumentException(
                    "El plazo es obligatorio."
            );
        }


        if (dto.getFechaInicio() == null) {

            throw new IllegalArgumentException(
                    "La fecha de desembolso es obligatoria."
            );
        }


        if (dto.getFechaPrimerPago() == null) {

            throw new IllegalArgumentException(
                    "La fecha del primer pago es obligatoria."
            );
        }


        boolean exigirGarantia =
                requiereGarantia(dto)
                        ||
                        hayDatosGarantia(
                                dto,
                                archivo
                        );


        /*
         * Las observaciones NO son obligatorias.
         */
        if (exigirGarantia) {

            if (vacio(dto.getTipoGarantia())) {

                throw new IllegalArgumentException(
                        "El tipo de garantía es obligatorio."
                );
            }


            if (vacio(dto.getDescripcionGarantia())) {

                throw new IllegalArgumentException(
                        "La descripción de la garantía es obligatoria."
                );
            }


            if (
                    dto.getValorEstimadoGarantia() == null
                            ||
                            dto.getValorEstimadoGarantia()
                                    .compareTo(
                                            BigDecimal.ZERO
                                    ) <= 0
            ) {

                throw new IllegalArgumentException(
                        "El valor de la garantía debe ser mayor que cero."
                );
            }


            if (
                    archivo == null
                            ||
                            archivo.isEmpty()
            ) {

                throw new IllegalArgumentException(
                        "Debes adjuntar el PDF o foto de la garantía."
                );
            }
        }
    }


    private boolean requiereGarantia(
            CreditoRegistroDto dto
    ) {

        return dto.getCapitalOriginal() != null
                &&
                dto.getCapitalOriginal()
                        .compareTo(
                                LIMITE_GARANTIA
                        ) >= 0;
    }


    /*
     * Observaciones no cuentan como inicio
     * de una garantía.
     */
    private boolean hayDatosGarantia(
            CreditoRegistroDto dto,
            MultipartFile archivo
    ) {

        return !vacio(
                dto.getTipoGarantia()
        )
                ||
                !vacio(
                        dto.getDescripcionGarantia()
                )
                ||
                dto.getValorEstimadoGarantia()
                        != null
                ||
                (
                        archivo != null
                                &&
                                !archivo.isEmpty()
                );
    }


    private boolean vacio(
            String valor
    ) {

        return valor == null
                ||
                valor.isBlank();
    }
}