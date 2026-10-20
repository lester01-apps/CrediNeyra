package com.credineyra.dto;

import java.math.BigDecimal;

public record ReporteSerieDto(
        String etiqueta,
        BigDecimal capital,
        BigDecimal interes
) {
}
