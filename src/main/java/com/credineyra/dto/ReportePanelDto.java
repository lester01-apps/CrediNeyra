package com.credineyra.dto;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;

public record ReportePanelDto(
        ReporteIndicadoresDto indicadores,
        List<ReporteSerieDto> recuperacionMensual,
        List<ReporteSerieDto> evolucionCreditos,
        List<ReporteEstadoDto> distribucionEstados,
        Page<ReporteFilaDto> filas,
        BigDecimal deudaVencida
) {
    public BigDecimal maximoRecuperacion() {
        return recuperacionMensual.stream()
                .map(serie -> seguro(serie.capital()).add(seguro(serie.interes())))
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ONE);
    }

    public long totalEstados() {
        return distribucionEstados.stream()
                .mapToLong(ReporteEstadoDto::cantidad)
                .sum();
    }

    private BigDecimal seguro(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }
}
