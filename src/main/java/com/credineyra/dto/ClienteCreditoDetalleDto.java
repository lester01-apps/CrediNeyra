package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ClienteCreditoDetalleDto(
        Long idCredito,
        String codigoCredito,
        String estado,
        BigDecimal capitalOriginal,
        BigDecimal capitalPendiente,
        BigDecimal interesPendiente,
        BigDecimal totalPendiente,
        BigDecimal montoExigible,
        Integer cuotaActual,
        LocalDate proximaCuota,
        LocalDate fechaInicio,
        Integer plazoMeses,
        Integer numeroCuotas,
        BigDecimal tasaInteres,
        String frecuenciaPago,
        List<CuotaFila> cuotas
) {
    public record CuotaFila(
            Integer numero,
            LocalDate vencimiento,
            BigDecimal capital,
            BigDecimal interes,
            BigDecimal monto,
            String estado
    ) {}
}
