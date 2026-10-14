package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreditoDetalleDto(

        Long idCredito,

        String codigoCredito,

        String estado,

        String fechaRegistro,

        String observaciones,


        // =====================================================
        // CLIENTE
        // =====================================================

        Long idCliente,

        String codigoCliente,

        String clienteNombre,

        String clienteDni,

        String clienteDireccion,


        // =====================================================
        // RESUMEN FINANCIERO
        // =====================================================

        BigDecimal capitalOriginal,

        BigDecimal capitalPagado,

        BigDecimal capitalPendiente,

        BigDecimal interesGenerado,

        BigDecimal interesPagado,

        BigDecimal interesPendiente,

        BigDecimal totalCredito,

        BigDecimal totalPagado,

        BigDecimal totalPendiente,


        // =====================================================
        // CONDICIONES CONGELADAS
        // =====================================================

        String tipoCalculo,

        BigDecimal tasaInteres,

        String periodicidadInteres,

        String frecuenciaPago,

        Integer plazoMeses,

        Integer numeroCuotas,

        LocalDate fechaInicio,

        LocalDate fechaPrimerPago,


        // =====================================================
        // GARANTÍA
        // =====================================================

        GarantiaDetalle garantia,


        // =====================================================
        // CUOTAS
        // =====================================================

        List<CuotaDetalle> cuotas,


        // =====================================================
        // DOCUMENTOS
        // =====================================================

        List<DocumentoDetalle> documentos

) {


    public record GarantiaDetalle(

            Long idGarantia,

            String tipo,

            String descripcion,

            BigDecimal valorEstimado,

            String estado,

            String fechaRegistro,

            String observaciones

    ) {
    }


    public record CuotaDetalle(

            Long idCuota,

            Integer numero,

            LocalDate fechaVencimiento,

            BigDecimal capitalOriginal,

            BigDecimal interesOriginal,

            BigDecimal totalOriginal,

            BigDecimal capitalPagado,

            BigDecimal interesPagado,

            BigDecimal capitalPendiente,

            BigDecimal interesPendiente,

            BigDecimal regularizacionPendiente,

            BigDecimal totalPendiente,

            String estado

    ) {
    }


    public record DocumentoDetalle(

            Long idDocumento,

            String codigoDocumento,

            String tipo,

            String nombreArchivo,

            String estado,

            String fechaSubida,

            String observaciones,

            boolean imagen,

            boolean garantia,

            boolean contratoGenerado,

            boolean contratoFirmado

    ) {
    }
}