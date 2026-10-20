package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ClientePagoPanelDto(

        String clienteNombre,

        String clienteDni,

        List<CreditoFila> creditos,

        List<PagoFila> pagos

) {

    public record CreditoFila(

            Long idCredito,

            String codigoCredito,

            String estado,

            BigDecimal capitalOriginal,

            BigDecimal capitalPendiente,

            BigDecimal interesPendiente,

            BigDecimal totalPendiente,

            Integer proximaCuota,

            LocalDate proximoVencimiento,

            BigDecimal montoExigible
    ) {
    }

    public record PagoFila(

            Long idPago,

            String codigoPago,

            String codigoCredito,

            BigDecimal monto,

            String referencia,

            String estado,

            OffsetDateTime fechaPago,

            Long idComprobante,

            Long idConstancia
    ) {
    }
}
