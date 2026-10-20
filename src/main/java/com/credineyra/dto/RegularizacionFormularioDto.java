package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegularizacionFormularioDto(

        Long idCredito,
        String codigoCredito,

        Long idCliente,
        String codigoCliente,
        String clienteNombre,
        String clienteDni,

        BigDecimal capitalPendiente,
        BigDecimal interesPendiente,
        BigDecimal totalPendiente,

        Integer cuotasVencidas,
        LocalDate vencimientoMasAntiguo,

        String estadoCredito

) {
}