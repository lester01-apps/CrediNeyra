package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoCredito;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CreditoClienteResumenDto {

    private Long idCredito;

    private String codigoCredito;

    private BigDecimal capitalOriginal =
            BigDecimal.ZERO.setScale(2);

    private BigDecimal capitalPendiente =
            BigDecimal.ZERO.setScale(2);

    private BigDecimal interesPendiente =
            BigDecimal.ZERO.setScale(2);

    private BigDecimal saldoPendiente =
            BigDecimal.ZERO.setScale(2);

    private EstadoCredito estado;

    private OffsetDateTime fechaSolicitud;


    public CreditoClienteResumenDto() {
    }


    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(
            Long idCredito
    ) {
        this.idCredito = idCredito;
    }


    public String getCodigoCredito() {
        return codigoCredito;
    }

    public void setCodigoCredito(
            String codigoCredito
    ) {
        this.codigoCredito = codigoCredito;
    }


    public BigDecimal getCapitalOriginal() {
        return capitalOriginal;
    }

    public void setCapitalOriginal(
            BigDecimal capitalOriginal
    ) {
        this.capitalOriginal = capitalOriginal;
    }


    public BigDecimal getCapitalPendiente() {
        return capitalPendiente;
    }

    public void setCapitalPendiente(
            BigDecimal capitalPendiente
    ) {
        this.capitalPendiente =
                capitalPendiente;
    }


    public BigDecimal getInteresPendiente() {
        return interesPendiente;
    }

    public void setInteresPendiente(
            BigDecimal interesPendiente
    ) {
        this.interesPendiente =
                interesPendiente;
    }


    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(
            BigDecimal saldoPendiente
    ) {
        this.saldoPendiente =
                saldoPendiente;
    }


    public EstadoCredito getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoCredito estado
    ) {
        this.estado = estado;
    }


    public OffsetDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(
            OffsetDateTime fechaSolicitud
    ) {
        this.fechaSolicitud =
                fechaSolicitud;
    }
}