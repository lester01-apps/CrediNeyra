package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PagoPanelDto(

        long totalPagos,
        long pendientes,
        long aprobados,
        long rechazados,

        BigDecimal totalAprobado,

        List<Fila> pagos

) {

    public record Fila(

            Long idPago,

            String codigoPago,

            String clienteNombre,

            String codigoCredito,

            BigDecimal monto,

            String metodoPago,

            String referencia,

            String estado,

            OffsetDateTime fechaPago,

            boolean tieneComprobante
    ) {
    }
}