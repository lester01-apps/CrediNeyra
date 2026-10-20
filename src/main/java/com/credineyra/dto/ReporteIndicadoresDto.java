package com.credineyra.dto;

import java.math.BigDecimal;

public record ReporteIndicadoresDto(
        long creditosActivos,
        long creditosEnMora,
        long cuotasVencidas,
        long pagosAprobados,
        BigDecimal capitalRecuperado,
        BigDecimal interesesCobrados,
        BigDecimal saldoPendiente,
        BigDecimal pagoAprobadoPeriodo
) {
}
