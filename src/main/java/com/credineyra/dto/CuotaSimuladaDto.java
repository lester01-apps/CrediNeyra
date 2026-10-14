package com.credineyra.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuotaSimuladaDto {

    private Integer numero;
    private LocalDate fechaVencimiento;

    private BigDecimal saldoInicial;
    private BigDecimal capital;
    private BigDecimal interes;
    private BigDecimal totalCuota;
    private BigDecimal saldoFinal;

    public CuotaSimuladaDto() {
    }

    public CuotaSimuladaDto(
            Integer numero,
            LocalDate fechaVencimiento,
            BigDecimal saldoInicial,
            BigDecimal capital,
            BigDecimal interes,
            BigDecimal totalCuota,
            BigDecimal saldoFinal
    ) {
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
        this.saldoInicial = saldoInicial;
        this.capital = capital;
        this.interes = interes;
        this.totalCuota = totalCuota;
        this.saldoFinal = saldoFinal;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(
            LocalDate fechaVencimiento
    ) {
        this.fechaVencimiento =
                fechaVencimiento;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(
            BigDecimal saldoInicial
    ) {
        this.saldoInicial = saldoInicial;
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getInteres() {
        return interes;
    }

    public void setInteres(BigDecimal interes) {
        this.interes = interes;
    }

    public BigDecimal getTotalCuota() {
        return totalCuota;
    }

    public void setTotalCuota(
            BigDecimal totalCuota
    ) {
        this.totalCuota = totalCuota;
    }

    public BigDecimal getSaldoFinal() {
        return saldoFinal;
    }

    public void setSaldoFinal(
            BigDecimal saldoFinal
    ) {
        this.saldoFinal = saldoFinal;
    }
}