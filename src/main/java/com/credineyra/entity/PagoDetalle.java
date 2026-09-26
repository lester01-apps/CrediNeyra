package com.credineyra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

@Entity
@Table(
        name = "pago_detalle",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pago_cuota",
                        columnNames = {"id_pago", "id_cuota"}
                )
        }
)
public class PagoDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pago", nullable = false)
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuota", nullable = false)
    private Cuota cuota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "monto_capital",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoCapital;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "monto_interes",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoInteres;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "monto_capital_regularizado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoCapitalRegularizado;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "monto_total",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoTotal;

    public PagoDetalle() {
    }

    @PrePersist
    @PreUpdate
    public void calcularTotal() {

        if (montoCapital == null) {
            montoCapital = BigDecimal.ZERO;
        }

        if (montoInteres == null) {
            montoInteres = BigDecimal.ZERO;
        }

        if (montoCapitalRegularizado == null) {
            montoCapitalRegularizado = BigDecimal.ZERO;
        }

        montoTotal =
                montoCapital
                        .add(montoInteres)
                        .add(montoCapitalRegularizado);
    }

    public Long getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Long idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Cuota getCuota() {
        return cuota;
    }

    public void setCuota(Cuota cuota) {
        this.cuota = cuota;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public BigDecimal getMontoCapital() {
        return montoCapital;
    }

    public void setMontoCapital(BigDecimal montoCapital) {
        this.montoCapital = montoCapital;
    }

    public BigDecimal getMontoInteres() {
        return montoInteres;
    }

    public void setMontoInteres(BigDecimal montoInteres) {
        this.montoInteres = montoInteres;
    }

    public BigDecimal getMontoCapitalRegularizado() {
        return montoCapitalRegularizado;
    }

    public void setMontoCapitalRegularizado(
            BigDecimal montoCapitalRegularizado
    ) {
        this.montoCapitalRegularizado = montoCapitalRegularizado;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }
}