package com.credineyra.dto;

import java.math.BigDecimal;

public class PagoAplicacionDto {

    private Long idCuota;

    private BigDecimal montoCapital =
            BigDecimal.ZERO;

    private BigDecimal montoInteres =
            BigDecimal.ZERO;

    private BigDecimal montoCapitalRegularizado =
            BigDecimal.ZERO;

    public PagoAplicacionDto() {
    }

    public Long getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }

    public BigDecimal getMontoCapital() {
        return montoCapital;
    }

    public void setMontoCapital(
            BigDecimal montoCapital
    ) {
        this.montoCapital = montoCapital;
    }

    public BigDecimal getMontoInteres() {
        return montoInteres;
    }

    public void setMontoInteres(
            BigDecimal montoInteres
    ) {
        this.montoInteres = montoInteres;
    }

    public BigDecimal getMontoCapitalRegularizado() {
        return montoCapitalRegularizado;
    }

    public void setMontoCapitalRegularizado(
            BigDecimal montoCapitalRegularizado
    ) {
        this.montoCapitalRegularizado =
                montoCapitalRegularizado;
    }
}