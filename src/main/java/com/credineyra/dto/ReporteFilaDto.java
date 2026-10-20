package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReporteFilaDto(
        Long id,
        String codigo,
        String cliente,
        String estado,
        LocalDate fecha,
        BigDecimal capital,
        BigDecimal recuperado,
        BigDecimal saldo
) {
}
