package com.credineyra.service;

import com.credineyra.dto.CreditoDetalleDto;
import com.credineyra.entity.*;
import com.credineyra.entity.enums.TipoDocumento;
import com.credineyra.repository.CreditoRepository;
import com.credineyra.repository.CuotaRepository;
import com.credineyra.repository.DocumentoRepository;
import com.credineyra.repository.GarantiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class CreditoDetalleService {

    private final CreditoRepository creditoRepository;

    private final CuotaRepository cuotaRepository;

    private final GarantiaRepository garantiaRepository;

    private final DocumentoRepository documentoRepository;


    public CreditoDetalleService(
            CreditoRepository creditoRepository,
            CuotaRepository cuotaRepository,
            GarantiaRepository garantiaRepository,
            DocumentoRepository documentoRepository
    ) {

        this.creditoRepository =
                creditoRepository;

        this.cuotaRepository =
                cuotaRepository;

        this.garantiaRepository =
                garantiaRepository;

        this.documentoRepository =
                documentoRepository;
    }


    /*
     * =========================================================
     * DETALLE COMPLETO
     * =========================================================
     */

    @Transactional(readOnly = true)
    public CreditoDetalleDto obtener(
            Long idCredito
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


        /*
         * =====================================================
         * CLIENTE
         * =====================================================
         */

        Cliente cliente =
                credito.getCliente();


        if (cliente == null) {

            throw new IllegalStateException(
                    "El crédito no tiene cliente asociado."
            );
        }


        Persona persona =
                cliente.getPersona();


        if (persona == null) {

            throw new IllegalStateException(
                    "El cliente no tiene datos personales asociados."
            );
        }


        String nombreCompleto =
                unirNombre(
                        persona.getNombres(),
                        persona.getApellidos()
                );


        /*
         * =====================================================
         * CONDICIÓN
         * =====================================================
         */

        CondicionCredito condicion =
                credito.getCondicion();


        if (condicion == null) {

            throw new IllegalStateException(
                    "El crédito no tiene condiciones financieras asociadas."
            );
        }


        /*
         * =====================================================
         * CUOTAS
         * =====================================================
         */

        List<CreditoDetalleDto.CuotaDetalle> cuotas =
                cuotaRepository
                        .findByCredito_IdCreditoOrderByNumeroCuotaAsc(
                                idCredito
                        )
                        .stream()
                        .map(
                                this::mapearCuota
                        )
                        .toList();


        /*
         * =====================================================
         * GARANTÍA
         * =====================================================
         */

        CreditoDetalleDto.GarantiaDetalle garantia =
                garantiaRepository
                        .findFirstByCredito_IdCreditoOrderByFechaRegistroDesc(
                                idCredito
                        )
                        .map(
                                this::mapearGarantia
                        )
                        .orElse(
                                null
                        );


        /*
         * =====================================================
         * DOCUMENTOS
         * =====================================================
         */

        List<CreditoDetalleDto.DocumentoDetalle> documentos =
                documentoRepository
                        .findByCredito_IdCreditoOrderByFechaSubidaDesc(
                                idCredito
                        )
                        .stream()
                        .map(
                                this::mapearDocumento
                        )
                        .toList();


        /*
         * =====================================================
         * TOTALES
         * =====================================================
         */

        BigDecimal capitalOriginal =
                ceroSiNull(
                        credito.getCapitalOriginal()
                );


        BigDecimal capitalPagado =
                ceroSiNull(
                        credito.getCapitalPagado()
                );


        BigDecimal capitalPendiente =
                ceroSiNull(
                        credito.getCapitalPendiente()
                );


        BigDecimal interesGenerado =
                ceroSiNull(
                        credito.getInteresGenerado()
                );


        BigDecimal interesPagado =
                ceroSiNull(
                        credito.getInteresPagado()
                );


        BigDecimal interesPendiente =
                ceroSiNull(
                        credito.getInteresPendiente()
                );


        BigDecimal totalCredito =
                capitalOriginal
                        .add(
                                interesGenerado
                        );


        BigDecimal totalPagado =
                capitalPagado
                        .add(
                                interesPagado
                        );


        BigDecimal totalPendiente =
                capitalPendiente
                        .add(
                                interesPendiente
                        );


        /*
         * =====================================================
         * DTO FINAL
         * =====================================================
         */

        return new CreditoDetalleDto(

                credito.getIdCredito(),

                credito.getCodigoCredito(),

                credito.getEstado() == null
                        ? "-"
                        : credito
                        .getEstado()
                        .name(),

                fechaHora(
                        credito.getFechaSolicitud()
                ),

                texto(
                        credito.getObservaciones()
                ),


                // CLIENTE

                cliente.getIdCliente(),

                cliente.getCodigoCliente(),

                nombreCompleto,

                texto(
                        persona.getDni()
                ),

                texto(
                        persona.getDireccion()
                ),


                // DINERO

                capitalOriginal,

                capitalPagado,

                capitalPendiente,

                interesGenerado,

                interesPagado,

                interesPendiente,

                totalCredito,

                totalPagado,

                totalPendiente,


                // CONDICIÓN

                condicion.getTipoCalculo() == null
                        ? "-"
                        : condicion
                        .getTipoCalculo()
                        .name(),

                ceroSiNull(
                        condicion.getTasaInteres()
                ),

                condicion.getPeriodicidadTasa() == null
                        ? "-"
                        : condicion
                        .getPeriodicidadTasa()
                        .name(),

                condicion.getFrecuenciaPago() == null
                        ? "-"
                        : condicion
                        .getFrecuenciaPago()
                        .name(),

                condicion.getPlazoMeses(),

                condicion.getNumeroCuotas(),

                condicion.getFechaInicio(),

                condicion.getFechaPrimerPago(),


                // GARANTÍA

                garantia,


                // CUOTAS

                cuotas,


                // DOCUMENTOS

                documentos
        );
    }


    /*
     * =========================================================
     * CUOTA
     * =========================================================
     */

    private CreditoDetalleDto.CuotaDetalle mapearCuota(
            Cuota cuota
    ) {

        BigDecimal capitalOriginal =
                ceroSiNull(
                        cuota.getCapitalOriginal()
                );


        BigDecimal interesOriginal =
                ceroSiNull(
                        cuota.getInteresOriginal()
                );


        BigDecimal capitalPagado =
                ceroSiNull(
                        cuota.getCapitalPagado()
                );


        BigDecimal interesPagado =
                ceroSiNull(
                        cuota.getInteresPagado()
                );


        BigDecimal capitalPendiente =
                ceroSiNull(
                        cuota.getCapitalPendiente()
                );


        BigDecimal interesPendiente =
                ceroSiNull(
                        cuota.getInteresPendiente()
                );


        BigDecimal regularizacionPendiente =
                ceroSiNull(
                        cuota.getCapitalRegularizadoPendiente()
                );


        BigDecimal totalOriginal =
                capitalOriginal
                        .add(
                                interesOriginal
                        );


        BigDecimal totalPendiente =
                capitalPendiente
                        .add(
                                interesPendiente
                        )
                        .add(
                                regularizacionPendiente
                        );


        return new CreditoDetalleDto.CuotaDetalle(

                cuota.getIdCuota(),

                cuota.getNumeroCuota(),

                cuota.getFechaVencimiento(),

                capitalOriginal,

                interesOriginal,

                totalOriginal,

                capitalPagado,

                interesPagado,

                capitalPendiente,

                interesPendiente,

                regularizacionPendiente,

                totalPendiente,

                cuota.getEstado() == null
                        ? "-"
                        : cuota
                        .getEstado()
                        .name()
        );
    }


    /*
     * =========================================================
     * GARANTÍA
     * =========================================================
     */

    private CreditoDetalleDto.GarantiaDetalle mapearGarantia(
            Garantia garantia
    ) {

        return new CreditoDetalleDto.GarantiaDetalle(

                garantia.getIdGarantia(),

                texto(
                        garantia.getTipoGarantia()
                ),

                texto(
                        garantia.getDescripcion()
                ),

                ceroSiNull(
                        garantia.getValorEstimado()
                ),

                garantia.getEstado() == null
                        ? "-"
                        : garantia
                        .getEstado()
                        .name(),

                fechaHora(
                        garantia.getFechaRegistro()
                ),

                texto(
                        garantia.getObservaciones()
                )
        );
    }


    /*
     * =========================================================
     * DOCUMENTO
     * =========================================================
     */

    private CreditoDetalleDto.DocumentoDetalle mapearDocumento(
            Documento documento
    ) {

        TipoDocumento tipo =
                documento.getTipoDocumento();


        String nombre =
                documento.getNombreArchivo() == null
                        ? ""
                        : documento
                        .getNombreArchivo()
                        .toLowerCase(
                                Locale.ROOT
                        );


        boolean imagen =
                nombre.endsWith(".jpg")
                        ||
                        nombre.endsWith(".jpeg")
                        ||
                        nombre.endsWith(".png")
                        ||
                        nombre.endsWith(".webp");


        boolean garantia =
                tipo ==
                        TipoDocumento.DOCUMENTO_GARANTIA
                        ||
                        tipo ==
                                TipoDocumento.FOTO_GARANTIA;


        boolean contratoGenerado =
                tipo ==
                        TipoDocumento.CONTRATO_GENERADO;


        boolean contratoFirmado =
                tipo ==
                        TipoDocumento.CONTRATO_FIRMADO;


        return new CreditoDetalleDto.DocumentoDetalle(

                documento.getIdDocumento(),

                texto(
                        documento.getCodigoDocumento()
                ),

                tipo == null
                        ? "-"
                        : tipo.name(),

                texto(
                        documento.getNombreArchivo()
                ),

                documento.getEstado() == null
                        ? "-"
                        : documento
                        .getEstado()
                        .name(),

                fechaHora(
                        documento.getFechaSubida()
                ),

                texto(
                        documento.getObservaciones()
                ),

                imagen,

                garantia,

                contratoGenerado,

                contratoFirmado
        );
    }


    /*
     * =========================================================
     * HELPERS
     * =========================================================
     */

    private BigDecimal ceroSiNull(
            BigDecimal valor
    ) {

        return valor == null
                ? BigDecimal.ZERO
                : valor;
    }


    private String unirNombre(
            String nombres,
            String apellidos
    ) {

        String resultado =
                (
                        textoVacio(nombres)
                                +
                                " "
                                +
                                textoVacio(apellidos)
                )
                        .trim();


        return resultado.isBlank()
                ? "Cliente"
                : resultado;
    }


    private String textoVacio(
            String valor
    ) {

        return valor == null
                ? ""
                : valor.trim();
    }


    private String texto(
            Object valor
    ) {

        if (valor == null) {
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


    private String fechaHora(
            OffsetDateTime fecha
    ) {

        if (fecha == null) {
            return "-";
        }


        return fecha.format(
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm"
                )
        );
    }
}