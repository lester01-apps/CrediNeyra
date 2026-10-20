package com.credineyra.service;

import com.credineyra.dto.PagoDetalleVistaDto;
import com.credineyra.entity.*;
import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.exception.RecursoNoEncontradoException;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.PagoDetalleRepository;
import com.credineyra.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class PagoDetalleVistaService {

    private final PagoRepository pagoRepository;

    private final PagoDetalleRepository pagoDetalleRepository;

    private final CuotaRepository cuotaRepository;

    private final DocumentoRepository documentoRepository;


    public PagoDetalleVistaService(
            PagoRepository pagoRepository,
            PagoDetalleRepository pagoDetalleRepository,
            CuotaRepository cuotaRepository,
            DocumentoRepository documentoRepository
    ) {

        this.pagoRepository =
                pagoRepository;

        this.pagoDetalleRepository =
                pagoDetalleRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.documentoRepository =
                documentoRepository;
    }


    /*
     * =========================================================
     * OBTENER DETALLE COMPLETO DEL PAGO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public PagoDetalleVistaDto obtener(
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


        Credito credito =
                pago.getCredito();


        if (credito == null) {

            throw new IllegalStateException(
                    "El pago no tiene crédito asociado"
            );
        }


        Cliente cliente =
                credito.getCliente();


        if (cliente == null) {

            throw new IllegalStateException(
                    "El crédito no tiene cliente asociado"
            );
        }


        Persona persona =
                cliente.getPersona();


        if (persona == null) {

            throw new IllegalStateException(
                    "El cliente no tiene datos personales asociados"
            );
        }


        /*
         * =====================================================
         * COMPROBANTE
         * =====================================================
         */

        Optional<Documento> comprobanteOptional =
                documentoRepository
                        .findFirstByPago_IdPagoAndTipoDocumentoAndEstadoOrderByFechaSubidaDesc(
                                idPago,
                                TipoDocumento.COMPROBANTE_PAGO,
                                EstadoDocumento.ACTIVO
                        );


        Long idDocumentoComprobante =
                comprobanteOptional
                        .map(
                                Documento::getIdDocumento
                        )
                        .orElse(
                                null
                        );


        String nombreComprobante =
                comprobanteOptional
                        .map(
                                Documento::getNombreArchivo
                        )
                        .orElse(
                                null
                        );


        boolean comprobanteImagen =
                nombreComprobante != null
                        &&
                        esImagen(
                                nombreComprobante
                        );


        /*
         * =====================================================
         * CUOTAS
         * =====================================================
         */

        LocalDate hoy =
                LocalDate.now();


        List<PagoDetalleVistaDto.CuotaPagoVistaDto> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                                credito.getIdCredito()
                        )
                        .stream()
                        .map(
                                cuota ->
                                        mapearCuota(
                                                cuota,
                                                hoy
                                        )
                        )
                        .toList();


        /*
         * =====================================================
         * APLICACIONES REALIZADAS
         * =====================================================
         */

        List<PagoDetalleVistaDto.AplicacionPagoVistaDto>
                aplicaciones =
                pagoDetalleRepository
                        .findByPago_IdPago(
                                idPago
                        )
                        .stream()
                        .map(
                                this::mapearAplicacion
                        )
                        .toList();


        /*
         * =====================================================
         * VALIDADOR
         * =====================================================
         */

        String validadoPor =
                "-";


        if (
                pago.getValidadoPor() != null
        ) {

            Usuario usuario =
                    pago.getValidadoPor();


            if (
                    usuario.getPersona() != null
            ) {

                validadoPor =
                        nombreCompleto(
                                usuario
                                        .getPersona()
                                        .getNombres(),

                                usuario
                                        .getPersona()
                                        .getApellidos()
                        );

            } else {

                validadoPor =
                        usuario.getUsername();
            }
        }


        /*
         * =====================================================
         * TOTALES
         * =====================================================
         */

        BigDecimal capitalPendiente =
                nz(
                        credito.getCapitalPendiente()
                );


        BigDecimal interesPendiente =
                nz(
                        credito.getInteresPendiente()
                );


        BigDecimal totalPendiente =
                capitalPendiente
                        .add(
                                interesPendiente
                        );


        /*
         * =====================================================
         * CÓDIGO VISUAL
         * =====================================================
         */

        String codigoPago =
                String.format(
                        "PAG-%06d",
                        pago.getIdPago()
                );


        /*
         * =====================================================
         * DTO FINAL
         * =====================================================
         */

        return new PagoDetalleVistaDto(

                pago.getIdPago(),

                codigoPago,

                pago.getEstado() == null
                        ? "-"
                        : pago
                        .getEstado()
                        .name(),

                pago.getFechaPago(),

                pago.getFechaValidacion(),


                // CLIENTE

                cliente.getIdCliente(),

                cliente.getCodigoCliente(),

                nombreCompleto(
                        persona.getNombres(),
                        persona.getApellidos()
                ),

                texto(
                        persona.getDni()
                ),


                // CRÉDITO

                credito.getIdCredito(),

                credito.getCodigoCredito(),

                capitalPendiente,

                interesPendiente,

                totalPendiente,

                credito.getCondicion() != null
                        &&
                        Boolean.TRUE.equals(
                                credito
                                        .getCondicion()
                                        .getPermitePagoSoloInteres()
                        ),


                // PAGO

                nz(
                        pago.getMonto()
                ),

                pago.getMetodoPago() == null
                        ? "-"
                        : pago
                        .getMetodoPago()
                        .name(),

                texto(
                        pago.getReferencia()
                ),

                texto(
                        pago.getObservaciones()
                ),


                // VALIDACIÓN

                validadoPor,


                // COMPROBANTE

                idDocumentoComprobante,

                nombreComprobante,

                comprobanteImagen,

                comprobanteOptional.isPresent(),


                // CUOTAS

                cuotas,


                // APLICACIONES

                aplicaciones
        );
    }


    /*
     * =========================================================
     * MAPEAR CUOTA
     * =========================================================
     */

    private PagoDetalleVistaDto.CuotaPagoVistaDto
    mapearCuota(
            Cuota cuota,
            LocalDate hoy
    ) {

        BigDecimal capitalPendiente =
                nz(
                        cuota.getCapitalPendiente()
                );


        BigDecimal interesPendiente =
                nz(
                        cuota.getInteresPendiente()
                );


        BigDecimal regularizadoPendiente =
                nz(
                        cuota.getCapitalRegularizadoPendiente()
                );


        BigDecimal totalPendiente =
                capitalPendiente
                        .add(
                                interesPendiente
                        )
                        .add(
                                regularizadoPendiente
                        );


        /*
         * Solo se puede aplicar un pago si:
         *
         * - la cuota ya venció o vence hoy;
         * - todavía tiene saldo.
         */
        boolean exigible =
                cuota.getFechaVencimiento() != null
                        &&
                        !cuota
                                .getFechaVencimiento()
                                .isAfter(hoy)
                        &&
                        totalPendiente
                                .compareTo(
                                        BigDecimal.ZERO
                                ) > 0;


        return new PagoDetalleVistaDto.CuotaPagoVistaDto(

                cuota.getIdCuota(),

                cuota.getNumeroCuota(),

                cuota.getFechaVencimiento(),

                capitalPendiente,

                interesPendiente,

                regularizadoPendiente,

                totalPendiente,

                cuota.getEstado() == null
                        ? "-"
                        : cuota
                        .getEstado()
                        .name(),

                exigible
        );
    }


    /*
     * =========================================================
     * MAPEAR APLICACIÓN
     * =========================================================
     */

    private PagoDetalleVistaDto.AplicacionPagoVistaDto
    mapearAplicacion(
            PagoDetalle detalle
    ) {

        Integer numeroCuota =
                detalle.getCuota() == null
                        ? null
                        : detalle
                        .getCuota()
                        .getNumeroCuota();


        return new PagoDetalleVistaDto.AplicacionPagoVistaDto(

                detalle.getIdDetalle(),

                numeroCuota,

                nz(
                        detalle.getMontoCapital()
                ),

                nz(
                        detalle.getMontoInteres()
                ),

                nz(
                        detalle.getMontoCapitalRegularizado()
                ),

                nz(
                        detalle.getMontoTotal()
                )
        );
    }


    /*
     * =========================================================
     * ES IMAGEN
     * =========================================================
     */

    private boolean esImagen(
            String nombre
    ) {

        String valor =
                nombre
                        .toLowerCase(
                                Locale.ROOT
                        );


        return valor.endsWith(".jpg")
                ||
                valor.endsWith(".jpeg")
                ||
                valor.endsWith(".png")
                ||
                valor.endsWith(".webp");
    }


    /*
     * =========================================================
     * NOMBRE COMPLETO
     * =========================================================
     */

    private String nombreCompleto(
            String nombres,
            String apellidos
    ) {

        String resultado =
                (
                        textoVacio(
                                nombres
                        )
                                +
                                " "
                                +
                                textoVacio(
                                        apellidos
                                )
                )
                        .trim();


        return resultado.isBlank()
                ? "Sin nombre"
                : resultado;
    }


    /*
     * =========================================================
     * NULL -> 0
     * =========================================================
     */

    private BigDecimal nz(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                .setScale(2)
                : valor;
    }


    /*
     * =========================================================
     * TEXTO
     * =========================================================
     */

    private String texto(
            Object valor
    ) {

        if (
                valor == null
        ) {

            return "-";
        }


        String resultado =
                valor
                        .toString()
                        .trim();


        return resultado.isBlank()
                ? "-"
                : resultado;
    }


    private String textoVacio(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }
}