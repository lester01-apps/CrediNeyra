package com.credineyra.entity;

import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "condicion_credito")
public class CondicionCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_condicion")
    private Long idCondicion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tasa_origen")
    private Tasa tasaOrigen;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_calculo",
            nullable = false,
            length = 30
    )
    private TipoCalculoCredito tipoCalculo;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "La tasa de interés no puede ser negativa"
    )
    @Digits(
            integer = 3,
            fraction = 2,
            message = "La tasa de interés debe tener como máximo 3 enteros y 2 decimales"
    )
    @Column(
            name = "tasa_interes",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal tasaInteres;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "periodicidad_tasa",
            nullable = false,
            length = 30
    )
    private Periodicidad periodicidadTasa;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "frecuencia_pago",
            nullable = false,
            length = 30
    )
    private Periodicidad frecuenciaPago;

    @Min(
            value = 1,
            message = "El plazo debe ser mayor a 0"
    )
    @Column(
            name = "plazo_meses",
            nullable = false
    )
    private Integer plazoMeses;

    @Min(
            value = 1,
            message = "El número de cuotas debe ser mayor a 0"
    )
    @Column(
            name = "numero_cuotas",
            nullable = false
    )
    private Integer numeroCuotas;

    @Column(
            name = "fecha_inicio",
            nullable = false
    )
    private LocalDate fechaInicio;

    @Column(
            name = "fecha_primer_pago",
            nullable = false
    )
    private LocalDate fechaPrimerPago;

    @Column(
            name = "permite_pago_solo_interes",
            nullable = false
    )
    private Boolean permitePagoSoloInteres;

    public CondicionCredito() {
    }

    @PrePersist
    public void prePersist() {

        if (permitePagoSoloInteres == null) {
            permitePagoSoloInteres = false;
        }
    }

    public Long getIdCondicion() {
        return idCondicion;
    }

    public void setIdCondicion(Long idCondicion) {
        this.idCondicion = idCondicion;
    }

    public Tasa getTasaOrigen() {
        return tasaOrigen;
    }

    public void setTasaOrigen(Tasa tasaOrigen) {
        this.tasaOrigen = tasaOrigen;
    }

    public TipoCalculoCredito getTipoCalculo() {
        return tipoCalculo;
    }

    public void setTipoCalculo(TipoCalculoCredito tipoCalculo) {
        this.tipoCalculo = tipoCalculo;
    }

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(BigDecimal tasaInteres) {
        this.tasaInteres = tasaInteres;
    }

    public Periodicidad getPeriodicidadTasa() {
        return periodicidadTasa;
    }

    public void setPeriodicidadTasa(Periodicidad periodicidadTasa) {
        this.periodicidadTasa = periodicidadTasa;
    }

    public Periodicidad getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public void setFrecuenciaPago(Periodicidad frecuenciaPago) {
        this.frecuenciaPago = frecuenciaPago;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public Integer getNumeroCuotas() {
        return numeroCuotas;
    }

    public void setNumeroCuotas(Integer numeroCuotas) {
        this.numeroCuotas = numeroCuotas;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaPrimerPago() {
        return fechaPrimerPago;
    }

    public void setFechaPrimerPago(LocalDate fechaPrimerPago) {
        this.fechaPrimerPago = fechaPrimerPago;
    }

    public Boolean getPermitePagoSoloInteres() {
        return permitePagoSoloInteres;
    }

    public void setPermitePagoSoloInteres(Boolean permitePagoSoloInteres) {
        this.permitePagoSoloInteres = permitePagoSoloInteres;
    }
}