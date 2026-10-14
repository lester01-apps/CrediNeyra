package com.credineyra.dto;

import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CreditoSimulacionDto {

    private BigDecimal capital;
    private BigDecimal tasaInteres;

    private TipoCalculoCredito tipoCalculo;
    private Periodicidad periodicidadInteres;
    private Periodicidad frecuenciaPago;

    private Integer plazoMeses;
    private Integer numeroCuotas;

    private BigDecimal interesTotal;
    private BigDecimal totalPagar;

    private BigDecimal primeraCuota;
    private BigDecimal ultimaCuota;

    private LocalDate fechaPrimerPago;
    private LocalDate fechaUltimoPago;

    private boolean garantiaObligatoria;

    private List<CuotaSimuladaDto> cronograma =
            new ArrayList<>();

    public CreditoSimulacionDto() {
    }

    public BigDecimal getCapital() {
        return capital;
    }

    public void setCapital(BigDecimal capital) {
        this.capital = capital;
    }

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(
            BigDecimal tasaInteres
    ) {
        this.tasaInteres = tasaInteres;
    }

    public TipoCalculoCredito getTipoCalculo() {
        return tipoCalculo;
    }

    public void setTipoCalculo(
            TipoCalculoCredito tipoCalculo
    ) {
        this.tipoCalculo = tipoCalculo;
    }

    public Periodicidad getPeriodicidadInteres() {
        return periodicidadInteres;
    }

    public void setPeriodicidadInteres(
            Periodicidad periodicidadInteres
    ) {
        this.periodicidadInteres =
                periodicidadInteres;
    }

    public Periodicidad getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public void setFrecuenciaPago(
            Periodicidad frecuenciaPago
    ) {
        this.frecuenciaPago = frecuenciaPago;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(
            Integer plazoMeses
    ) {
        this.plazoMeses = plazoMeses;
    }

    public Integer getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(
            Integer numeroCuotas
    ) {
        this.numeroCuotas = numeroCuotas;
    }

    public BigDecimal getInteresTotal() {
        return interesTotal;
    }

    public void setInteresTotal(
            BigDecimal interesTotal
    ) {
        this.interesTotal = interesTotal;
    }

    public BigDecimal getTotalPagar() {
        return totalPagar;
    }

    public void setTotalPagar(
            BigDecimal totalPagar
    ) {
        this.totalPagar = totalPagar;
    }

    public BigDecimal getPrimeraCuota() {
        return primeraCuota;
    }

    public void setPrimeraCuota(
            BigDecimal primeraCuota
    ) {
        this.primeraCuota = primeraCuota;
    }

    public BigDecimal getUltimaCuota() {
        return ultimaCuota;
    }

    public void setUltimaCuota(
            BigDecimal ultimaCuota
    ) {
        this.ultimaCuota = ultimaCuota;
    }

    public LocalDate getFechaPrimerPago() {
        return fechaPrimerPago;
    }

    public void setFechaPrimerPago(
            LocalDate fechaPrimerPago
    ) {
        this.fechaPrimerPago = fechaPrimerPago;
    }

    public LocalDate getFechaUltimoPago() {
        return fechaUltimoPago;
    }

    public void setFechaUltimoPago(
            LocalDate fechaUltimoPago
    ) {
        this.fechaUltimoPago =
                fechaUltimoPago;
    }

    public boolean isGarantiaObligatoria() {
        return garantiaObligatoria;
    }

    public void setGarantiaObligatoria(
            boolean garantiaObligatoria
    ) {
        this.garantiaObligatoria =
                garantiaObligatoria;
    }

    public List<CuotaSimuladaDto> getCronograma() {
        return cronograma;
    }

    public void setCronograma(
            List<CuotaSimuladaDto> cronograma
    ) {
        this.cronograma = cronograma;
    }
}