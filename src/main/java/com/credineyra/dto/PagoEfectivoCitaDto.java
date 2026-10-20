package com.credineyra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Solicitud de cita para que el cliente pague personalmente en efectivo. */
public class PagoEfectivoCitaDto {

    @NotNull(message = "Debe indicar el crédito")
    private Long idCredito;

    @NotNull(message = "Debe indicar el monto que pagará")
    @DecimalMin(value = "1.00", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @Size(max = 20, message = "El tipo de pago no es válido")
    private String tipoPago;

    @NotNull(message = "Seleccione la fecha de la cita")
    @FutureOrPresent(message = "La cita no puede ser en una fecha pasada")
    private LocalDate fecha;

    @NotNull(message = "Seleccione la hora de la cita")
    private LocalTime hora;

    @Size(max = 30, message = "El lugar de pago no es válido")
    private String lugarPago;

    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String direccion;

    @Size(max = 120, message = "La referencia no puede superar 120 caracteres")
    private String referenciaDireccion;

    @Size(max = 120, message = "La observación no puede superar 120 caracteres")
    private String observaciones;

    public Long getIdCredito() { return idCredito; }
    public void setIdCredito(Long idCredito) { this.idCredito = idCredito; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getTipoPago() { return tipoPago; }
    public void setTipoPago(String tipoPago) { this.tipoPago = tipoPago; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }
    public String getLugarPago() { return lugarPago; }
    public void setLugarPago(String lugarPago) { this.lugarPago = lugarPago; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getReferenciaDireccion() { return referenciaDireccion; }
    public void setReferenciaDireccion(String referenciaDireccion) { this.referenciaDireccion = referenciaDireccion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
