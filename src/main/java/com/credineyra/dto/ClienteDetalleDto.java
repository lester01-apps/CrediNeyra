package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.SituacionPagoCliente;
import com.credineyra.entity.enums.TipoOrigenCliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteDetalleDto {

    private Long idCliente;

    private String codigoCliente;

    private TipoOrigenCliente tipoOrigen;

    private EstadoCliente estado;

    private OffsetDateTime fechaRegistro;

    private String observaciones;


    /* DATOS PERSONALES */

    private String dni;

    private String nombres;

    private String apellidos;

    private String nombreCompleto;

    private String telefono;

    private String email;

    private String direccion;


    /* SITUACION FINANCIERA */

    private SituacionPagoCliente situacionPago;

    private LocalDate fechaProximaCuota;

    private BigDecimal montoProximaCuota =
            BigDecimal.ZERO.setScale(2);

    private BigDecimal saldoPendienteTotal =
            BigDecimal.ZERO.setScale(2);

    private long totalCreditos;

    private long creditosActivos;


    /* CREDITOS */

    private List<CreditoClienteResumenDto> creditos =
            new ArrayList<>();


    public ClienteDetalleDto() {
    }


    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }


    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }


    public TipoOrigenCliente getTipoOrigen() {
        return tipoOrigen;
    }

    public void setTipoOrigen(
            TipoOrigenCliente tipoOrigen
    ) {
        this.tipoOrigen = tipoOrigen;
    }


    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoCliente estado
    ) {
        this.estado = estado;
    }


    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(
            OffsetDateTime fechaRegistro
    ) {
        this.fechaRegistro = fechaRegistro;
    }


    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }


    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }


    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }


    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(
            String apellidos
    ) {
        this.apellidos = apellidos;
    }


    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(
            String nombreCompleto
    ) {
        this.nombreCompleto = nombreCompleto;
    }


    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(
            String telefono
    ) {
        this.telefono = telefono;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }


    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(
            String direccion
    ) {
        this.direccion = direccion;
    }


    public SituacionPagoCliente getSituacionPago() {
        return situacionPago;
    }

    public void setSituacionPago(
            SituacionPagoCliente situacionPago
    ) {
        this.situacionPago = situacionPago;
    }


    public LocalDate getFechaProximaCuota() {
        return fechaProximaCuota;
    }

    public void setFechaProximaCuota(
            LocalDate fechaProximaCuota
    ) {
        this.fechaProximaCuota =
                fechaProximaCuota;
    }


    public BigDecimal getMontoProximaCuota() {
        return montoProximaCuota;
    }

    public void setMontoProximaCuota(
            BigDecimal montoProximaCuota
    ) {
        this.montoProximaCuota =
                montoProximaCuota;
    }


    public BigDecimal getSaldoPendienteTotal() {
        return saldoPendienteTotal;
    }

    public void setSaldoPendienteTotal(
            BigDecimal saldoPendienteTotal
    ) {
        this.saldoPendienteTotal =
                saldoPendienteTotal;
    }


    public long getTotalCreditos() {
        return totalCreditos;
    }

    public void setTotalCreditos(
            long totalCreditos
    ) {
        this.totalCreditos =
                totalCreditos;
    }


    public long getCreditosActivos() {
        return creditosActivos;
    }

    public void setCreditosActivos(
            long creditosActivos
    ) {
        this.creditosActivos =
                creditosActivos;
    }


    public List<CreditoClienteResumenDto> getCreditos() {
        return creditos;
    }

    public void setCreditos(
            List<CreditoClienteResumenDto> creditos
    ) {
        this.creditos = creditos;
    }
}