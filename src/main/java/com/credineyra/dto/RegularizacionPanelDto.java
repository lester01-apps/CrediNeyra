package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RegularizacionPanelDto(

        long periodoRegularizacion,

        long enMora,

        long atrasoProlongado,

        long planesActivos,

        List<Fila> creditos

) {

    public record Fila(

            Long idCredito,

            String codigoCredito,

            String clienteNombre,

            BigDecimal deudaPendiente,

            LocalDate vencimientoMasAntiguo,

            long diasAtraso,

            String situacion,

            boolean tienePlanActivo
    ) {
    }
}