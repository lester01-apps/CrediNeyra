package com.credineyra.dto;

import java.util.Arrays;

/** Tipos de informe disponibles para el panel administrativo. */
public enum TipoReporte {
    GENERAL("Reporte general de créditos"),
    CREDITOS_ACTIVOS("Créditos activos"),
    CREDITOS_EN_MORA("Créditos vencidos o en mora"),
    CUOTAS_PENDIENTES("Cuotas pendientes y vencidas"),
    PAGOS_APROBADOS("Pagos aprobados por período"),
    CAPITAL_PRESTADO_RECUPERADO("Capital prestado y recuperado"),
    SALDOS_PENDIENTES("Saldos pendientes"),
    CLIENTES_CON_DEUDA("Clientes con créditos y deudas"),
    REGULARIZACIONES("Regularizaciones"),
    RENDIMIENTO_FINANCIERO("Rendimiento financiero");

    private final String etiqueta;

    TipoReporte(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static TipoReporte desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return GENERAL;
        }
        return Arrays.stream(values())
                .filter(tipo -> tipo.name().equalsIgnoreCase(valor))
                .findFirst()
                .orElse(GENERAL);
    }
}
