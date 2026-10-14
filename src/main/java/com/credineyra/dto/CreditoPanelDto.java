package com.credineyra.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CreditoPanelDto {

    private long totalCreditos;
    private long creditosActivos;
    private long creditosAlDia;
    private long creditosPorVencer;
    private long creditosEnMora;
    private long atrasoProlongado;

    private BigDecimal capitalPrestado;
    private BigDecimal capitalRecuperado;
    private BigDecimal interesGenerado;
    private BigDecimal saldoPendiente;

    private List<CreditoResumenDto> creditos =
            new ArrayList<>();

    /*
     * Gráfico de pagos de los últimos 6 meses.
     */
    private List<String> mesesGrafico =
            new ArrayList<>();

    private List<BigDecimal> pagosGrafico =
            new ArrayList<>();

    public CreditoPanelDto() {
    }

    public long getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(long totalCreditos) {
        this.totalCreditos = totalCreditos;
    }

    public long getCreditosActivos() {
        return creditosActivos;
    }

    public void setCreditosActivos(long creditosActivos) {
        this.creditosActivos = creditosActivos;
    }

    public long getCreditosAlDia() {
        return creditosAlDia;
    }

    public void setCreditosAlDia(long creditosAlDia) {
        this.creditosAlDia = creditosAlDia;
    }

    public long getCreditosPorVencer() {
        return creditosPorVencer;
    }

    public void setCreditosPorVencer(long creditosPorVencer) {
        this.creditosPorVencer = creditosPorVencer;
    }

    public long getCreditosEnMora() {
        return creditosEnMora;
    }

    public void setCreditosEnMora(long creditosEnMora) {
        this.creditosEnMora = creditosEnMora;
    }

    public long getAtrasoProlongado() {
        return atrasoProlongado;
    }

    public void setAtrasoProlongado(long atrasoProlongado) {
        this.atrasoProlongado = atrasoProlongado;
    }

    public BigDecimal getCapitalPrestado() {
        return capitalPrestado;
    }

    public void setCapitalPrestado(BigDecimal capitalPrestado) {
        this.capitalPrestado = capitalPrestado;
    }

    public BigDecimal getCapitalRecuperado() {
        return capitalRecuperado;
    }

    public void setCapitalRecuperado(BigDecimal capitalRecuperado) {
        this.capitalRecuperado = capitalRecuperado;
    }

    public BigDecimal getInteresGenerado() {
        return interesGenerado;
    }

    public void setInteresGenerado(BigDecimal interesGenerado) {
        this.interesGenerado = interesGenerado;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public List<CreditoResumenDto> getCreditos() {
        return creditos;
    }

    public void setCreditos(List<CreditoResumenDto> creditos) {
        this.creditos = creditos;
    }

    public List<String> getMesesGrafico() {
        return mesesGrafico;
    }

    public void setMesesGrafico(List<String> mesesGrafico) {
        this.mesesGrafico = mesesGrafico;
    }

    public List<BigDecimal> getPagosGrafico() {
        return pagosGrafico;
    }

    public void setPagosGrafico(List<BigDecimal> pagosGrafico) {
        this.pagosGrafico = pagosGrafico;
    }
}