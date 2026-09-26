package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoCredito;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "credito",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credito_cliente",
                        columnNames = {"id_credito", "id_cliente"}
                )
        }
)
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credito")
    private Long idCredito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_cliente",
            nullable = false
    )
    private Cliente cliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_condicion",
            nullable = false,
            unique = true
    )
    private CondicionCredito condicion;

    @NotBlank(message = "El código del crédito es obligatorio")
    @Size(
            max = 20,
            message = "El código del crédito no puede superar los 20 caracteres"
    )
    @Column(
            name = "codigo_credito",
            nullable = false,
            unique = true,
            length = 20
    )
    private String codigoCredito;

    @Column(
            name = "fecha_solicitud",
            nullable = false
    )
    private OffsetDateTime fechaSolicitud;

    @DecimalMin(
            value = "0.01",
            message = "El capital original debe ser mayor a 0"
    )
    @Column(
            name = "capital_original",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalOriginal;

    @DecimalMin(
            value = "0.00",
            message = "El capital pagado no puede ser negativo"
    )
    @Column(
            name = "capital_pagado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalPagado;

    @DecimalMin(
            value = "0.00",
            message = "El capital pendiente no puede ser negativo"
    )
    @Column(
            name = "capital_pendiente",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalPendiente;

    @DecimalMin(
            value = "0.00",
            message = "El interés generado no puede ser negativo"
    )
    @Column(
            name = "interes_generado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesGenerado;

    @DecimalMin(
            value = "0.00",
            message = "El interés pagado no puede ser negativo"
    )
    @Column(
            name = "interes_pagado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesPagado;

    @DecimalMin(
            value = "0.00",
            message = "El interés pendiente no puede ser negativo"
    )
    @Column(
            name = "interes_pendiente",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesPendiente;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 30
    )
    private EstadoCredito estado;

    @Size(
            max = 200,
            message = "Las observaciones no pueden superar los 200 caracteres"
    )
    @Column(
            name = "observaciones",
            length = 200
    )
    private String observaciones;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Integer version;

    public Credito() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaSolicitud == null) {
            fechaSolicitud = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = EstadoCredito.ACTIVO;
        }

        if (capitalPagado == null) {
            capitalPagado = BigDecimal.ZERO;
        }

        if (interesPagado == null) {
            interesPagado = BigDecimal.ZERO;
        }

        normalizarMontos();
    }

    @PreUpdate
    public void preUpdate() {
        normalizarMontos();
    }

    private void normalizarMontos() {

        if (capitalOriginal != null) {
            capitalOriginal = redondearASol(capitalOriginal);
        }

        if (capitalPagado != null) {
            capitalPagado = redondearASol(capitalPagado);
        }

        if (capitalOriginal != null && capitalPagado != null) {
            capitalPendiente =
                    capitalOriginal.subtract(capitalPagado);
        }

        if (interesGenerado != null) {
            interesGenerado = redondearASol(interesGenerado);
        }

        if (interesPagado != null) {
            interesPagado = redondearASol(interesPagado);
        }

        if (interesGenerado != null && interesPagado != null) {
            interesPendiente =
                    interesGenerado.subtract(interesPagado);
        }
    }

    private BigDecimal redondearASol(BigDecimal monto) {

        return monto
                .setScale(0, RoundingMode.HALF_UP)
                .setScale(2);
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CondicionCredito getCondicion() {
        return condicion;
    }

    public void setCondicion(CondicionCredito condicion) {
        this.condicion = condicion;
    }

    public String getCodigoCredito() {
        return codigoCredito;
    }

    public void setCodigoCredito(String codigoCredito) {
        this.codigoCredito = codigoCredito;
    }

    public OffsetDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(OffsetDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public BigDecimal getCapitalOriginal() {
        return capitalOriginal;
    }

    public void setCapitalOriginal(BigDecimal capitalOriginal) {
        this.capitalOriginal = capitalOriginal;
    }

    public BigDecimal getCapitalPagado() {
        return capitalPagado;
    }

    public void setCapitalPagado(BigDecimal capitalPagado) {
        this.capitalPagado = capitalPagado;
    }

    public BigDecimal getCapitalPendiente() {
        return capitalPendiente;
    }

    public void setCapitalPendiente(BigDecimal capitalPendiente) {
        this.capitalPendiente = capitalPendiente;
    }

    public BigDecimal getInteresGenerado() {
        return interesGenerado;
    }

    public void setInteresGenerado(BigDecimal interesGenerado) {
        this.interesGenerado = interesGenerado;
    }

    public BigDecimal getInteresPagado() {
        return interesPagado;
    }

    public void setInteresPagado(BigDecimal interesPagado) {
        this.interesPagado = interesPagado;
    }

    public BigDecimal getInteresPendiente() {
        return interesPendiente;
    }

    public void setInteresPendiente(BigDecimal interesPendiente) {
        this.interesPendiente = interesPendiente;
    }

    public EstadoCredito getEstado() {
        return estado;
    }

    public void setEstado(EstadoCredito estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getVersion() {
        return version;
    }
}