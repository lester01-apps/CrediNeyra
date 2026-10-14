package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoCredito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class CreditoResumenDto {

    private Long idCredito;
    private Long idCliente;

    private String codigoCredito;
    private String codigoCliente;

    private String nombreCliente;
    private String dni;

    private BigDecimal capitalOriginal;
    private BigDecimal capitalPendiente;
    private BigDecimal interesPendiente;
    private BigDecimal totalPendiente;

    private String tipoInteres;
    private String periodicidadInteres;
    private String frecuenciaPago;

    private Integer plazoMeses;

    private LocalDate proximaFechaPago;
    private BigDecimal proximoMontoPago;

    private EstadoCredito estado;
    private String estadoVisual;

    private OffsetDateTime fechaSolicitud;

    public CreditoResumenDto() {
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getCodigoCredito() {
        return codigoCredito;
    }

    public void setCodigoCredito(String codigoCredito) {
        this.codigoCredito = codigoCredito;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public BigDecimal getCapitalOriginal() {
        return capitalOriginal;
    }

    public void setCapitalOriginal(BigDecimal capitalOriginal) {
        this.capitalOriginal = capitalOriginal;
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

    public BigDecimal getTotalPendiente() {
        return totalPendiente;
    }

    public void setTotalPendiente(BigDecimal totalPendiente) {
        this.totalPendiente = totalPendiente;
    }

    public String getTipoInteres() {
        return tipoInteres;
    }

    public void setTipoInteres(String tipoInteres) {
        this.tipoInteres = tipoInteres;
    }

    public String getPeriodicidadInteres() {
        return periodicidadInteres;
    }

    public void setPeriodicidadInteres(String periodicidadInteres) {
        this.periodicidadInteres = periodicidadInteres;
    }

    public String getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public void setFrecuenciaPago(String frecuenciaPago) {
        this.frecuenciaPago = frecuenciaPago;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public LocalDate getProximaFechaPago() {
        return proximaFechaPago;
    }

    public void setProximaFechaPago(LocalDate proximaFechaPago) {
        this.proximaFechaPago = proximaFechaPago;
    }

    public BigDecimal getProximoMontoPago() {
        return proximoMontoPago;
    }

    public void setProximoMontoPago(BigDecimal proximoMontoPago) {
        this.proximoMontoPago = proximoMontoPago;
    }

    public EstadoCredito getEstado() {
        return estado;
    }

    public void setEstado(EstadoCredito estado) {
        this.estado = estado;
    }

    public String getEstadoVisual() {
        return estadoVisual;
    }

    public void setEstadoVisual(String estadoVisual) {
        this.estadoVisual = estadoVisual;
    }

    public OffsetDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(OffsetDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
}