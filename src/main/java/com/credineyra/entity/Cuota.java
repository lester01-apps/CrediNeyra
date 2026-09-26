package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoCuota;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "cuota",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_credito_numero_cuota",
                        columnNames = {
                                "id_credito",
                                "numero_cuota"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_cuota_credito",
                        columnNames = {
                                "id_cuota",
                                "id_credito"
                        }
                )
        }
)
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuota")
    private Long idCuota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @Min(1)
    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_original",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalOriginal;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "interes_original",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesOriginal;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_pagado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalPagado;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "interes_pagado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesPagado;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_pendiente",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalPendiente;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "interes_pendiente",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesPendiente;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_regularizado_asignado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalRegularizadoAsignado;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_regularizado_pagado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalRegularizadoPagado;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_regularizado_pendiente",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalRegularizadoPendiente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCuota estado;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    public Cuota() {
    }

    @PrePersist
    public void prePersist() {

        if (capitalPagado == null) {
            capitalPagado = BigDecimal.ZERO;
        }

        if (interesPagado == null) {
            interesPagado = BigDecimal.ZERO;
        }

        if (capitalRegularizadoAsignado == null) {
            capitalRegularizadoAsignado = BigDecimal.ZERO;
        }

        if (capitalRegularizadoPagado == null) {
            capitalRegularizadoPagado = BigDecimal.ZERO;
        }

        if (capitalOriginal != null && capitalPendiente == null) {
            capitalPendiente = capitalOriginal;
        }

        if (interesOriginal != null && interesPendiente == null) {
            interesPendiente = interesOriginal;
        }

        if (capitalRegularizadoPendiente == null) {
            capitalRegularizadoPendiente =
                    capitalRegularizadoAsignado;
        }

        if (estado == null) {
            estado = EstadoCuota.PENDIENTE;
        }
    }

    public Long getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
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

    public Integer getVersion() {
        return version;
    }
}