package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoCuota;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CuotaDto {

    private Long idCuota;
    private Long idCredito;
    private Integer numeroCuota;

    private LocalDate fechaVencimiento;

    private BigDecimal capitalOriginal;
    private BigDecimal interesOriginal;

    private BigDecimal capitalPagado;
    private BigDecimal interesPagado;

    private BigDecimal capitalPendiente;
    private BigDecimal interesPendiente;

    private BigDecimal capitalRegularizadoAsignado;
    private BigDecimal capitalRegularizadoPagado;
    private BigDecimal capitalRegularizadoPendiente;

    private EstadoCuota estado;

    public CuotaDto() {
    }

    public Long getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Integer getNumeroCuota() {
        return numeroCuota;
    }

    public void setNumeroCuota(Integer numeroCuota) {
        this.numeroCuota = numeroCuota;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public BigDecimal getCapitalOriginal() {
        return capitalOriginal;
    }

    public void setCapitalOriginal(BigDecimal capitalOriginal) {
        this.capitalOriginal = capitalOriginal;
    }

    public BigDecimal getInteresOriginal() {
        return interesOriginal;
    }

    public void setInteresOriginal(BigDecimal interesOriginal) {
        this.interesOriginal = interesOriginal;
    }

    public BigDecimal getCapitalPagado() {
        return capitalPagado;
    }

    public void setCapitalPagado(BigDecimal capitalPagado) {
        this.capitalPagado = capitalPagado;
    }

    public BigDecimal getInteresPagado() {
        return interesPagado;
    }

    public void setInteresPagado(BigDecimal interesPagado) {
        this.interesPagado = interesPagado;
    }

    public BigDecimal getCapitalPendiente() {
        return capitalPendiente;
    }

    public void setCapitalPendiente(BigDecimal capitalPendiente) {
        this.capitalPendiente = capitalPendiente;
    }

    public BigDecimal getInteresPendiente() {
        return interesPendiente;
    }

    public void setInteresPendiente(BigDecimal interesPendiente) {
        this.interesPendiente = interesPendiente;
    }

    public BigDecimal getCapitalRegularizadoAsignado() {
        return capitalRegularizadoAsignado;
    }

    public void setCapitalRegularizadoAsignado(
            BigDecimal capitalRegularizadoAsignado
    ) {
        this.capitalRegularizadoAsignado =
                capitalRegularizadoAsignado;
    }

    public BigDecimal getCapitalRegularizadoPagado() {
        return capitalRegularizadoPagado;
    }

    public void setCapitalRegularizadoPagado(
            BigDecimal capitalRegularizadoPagado
    ) {
        this.capitalRegularizadoPagado =
                capitalRegularizadoPagado;
    }

    public BigDecimal getCapitalRegularizadoPendiente() {
        return capitalRegularizadoPendiente;
    }

    public void setCapitalRegularizadoPendiente(
            BigDecimal capitalRegularizadoPendiente
    ) {
        this.capitalRegularizadoPendiente =
                capitalRegularizadoPendiente;
    }

    public EstadoCuota getEstado() {
        return estado;
    }

    public void setEstado(EstadoCuota estado) {
        this.estado = estado;
    }
}