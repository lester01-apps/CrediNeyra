package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.SituacionPagoCliente;
import com.credineyra.entity.enums.TipoOrigenCliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class ClienteResumenDto {

    private Long idCliente;

    private String codigoCliente;

    private String nombreCompleto;

    private String dni;

    private String telefono;

    private String email;

    private TipoOrigenCliente tipoOrigen;

    private EstadoCliente estado;

    private OffsetDateTime fechaRegistro;

    private LocalDate fechaCuotaReferencia;

    private BigDecimal montoCuotaReferencia;

    private SituacionPagoCliente situacionPago;


    public ClienteResumenDto() {
    }


    public Long getIdCliente() {
        return idCliente;
    }


    public void setIdCliente(
            Long idCliente
    ) {

        this.idCliente =
                idCliente;
    }


    public String getCodigoCliente() {
        return codigoCliente;
    }


    public void setCodigoCliente(
            String codigoCliente
    ) {

        this.codigoCliente =
                codigoCliente;
    }


    public String getNombreCompleto() {
        return nombreCompleto;
    }


    public void setNombreCompleto(
            String nombreCompleto
    ) {

        this.nombreCompleto =
                nombreCompleto;
    }


    public String getDni() {
        return dni;
    }


    public void setDni(
            String dni
    ) {

        this.dni =
                dni;
    }


    public String getTelefono() {
        return telefono;
    }


    public void setTelefono(
            String telefono
    ) {

        this.telefono =
                telefono;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(
            String email
    ) {

        this.email =
                email;
    }


    public TipoOrigenCliente getTipoOrigen() {
        return tipoOrigen;
    }


    public void setTipoOrigen(
            TipoOrigenCliente tipoOrigen
    ) {

        this.tipoOrigen =
                tipoOrigen;
    }


    public EstadoCliente getEstado() {
        return estado;
    }


    public void setEstado(
            EstadoCliente estado
    ) {

        this.estado =
                estado;
    }


    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }


    public void setFechaRegistro(
            OffsetDateTime fechaRegistro
    ) {

        this.fechaRegistro =
                fechaRegistro;
    }


    public LocalDate getFechaCuotaReferencia() {
        return fechaCuotaReferencia;
    }


    public void setFechaCuotaReferencia(
            LocalDate fechaCuotaReferencia
    ) {

        this.fechaCuotaReferencia =
                fechaCuotaReferencia;
    }


    public BigDecimal getMontoCuotaReferencia() {
        return montoCuotaReferencia;
    }


    public void setMontoCuotaReferencia(
            BigDecimal montoCuotaReferencia
    ) {

        this.montoCuotaReferencia =
                montoCuotaReferencia;
    }


    public SituacionPagoCliente getSituacionPago() {
        return situacionPago;
    }


    public void setSituacionPago(
            SituacionPagoCliente situacionPago
    ) {

        this.situacionPago =
                situacionPago;
    }

}