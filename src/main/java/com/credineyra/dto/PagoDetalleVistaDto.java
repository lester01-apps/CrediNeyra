package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record PagoDetalleVistaDto(

        Long idPago,

        String codigoPago,

        String estado,

        OffsetDateTime fechaPago,

        OffsetDateTime fechaValidacion,


        // =====================================================
        // CLIENTE
        // =====================================================

        Long idCliente,

        String codigoCliente,

        String clienteNombre,

        String clienteDni,


        // =====================================================
        // CRÉDITO
        // =====================================================

        Long idCredito,

        String codigoCredito,

        BigDecimal capitalPendienteCredito,

        BigDecimal interesPendienteCredito,

        BigDecimal totalPendienteCredito,

        Boolean permitePagoSoloInteres,


        // =====================================================
        // PAGO PRESENTADO
        // =====================================================

        BigDecimal monto,

        String metodoPago,

        String referencia,

        String observaciones,


        // =====================================================
        // VALIDACIÓN
        // =====================================================

        String validadoPor,


        // =====================================================
        // COMPROBANTE DEL CLIENTE
        // =====================================================

        Long idDocumentoComprobante,

        String nombreComprobante,

        boolean comprobanteImagen,

        boolean tieneComprobante,


        // =====================================================
        // CUOTAS
        // =====================================================

        List<CuotaPagoVistaDto> cuotas,


        // =====================================================
        // APLICACIONES SI YA FUE APROBADO
        // =====================================================

        List<AplicacionPagoVistaDto> aplicaciones

) {


    /*
     * =========================================================
     * CUOTA DISPONIBLE PARA APLICAR PAGO
     * =========================================================
     */

    public record CuotaPagoVistaDto(

            Long idCuota,

            Integer numeroCuota,

            LocalDate fechaVencimiento,

            BigDecimal capitalPendiente,

            BigDecimal interesPendiente,

            BigDecimal capitalRegularizadoPendiente,

            BigDecimal totalPendiente,

            String estado,

            boolean exigible

    ) {
    }


    /*
     * =========================================================
     * DETALLE DE UN PAGO YA APROBADO
     * =========================================================
     */

    public record AplicacionPagoVistaDto(

            Long idDetalle,

            Integer numeroCuota,

            BigDecimal montoCapital,

            BigDecimal montoInteres,

            BigDecimal montoCapitalRegularizado,

            BigDecimal montoTotal

    ) {
    }
}