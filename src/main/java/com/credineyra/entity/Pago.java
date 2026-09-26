package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoPago;
import com.credineyra.entity.enums.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "pago",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pago_credito",
                        columnNames = {"id_pago", "id_credito"}
                )
        }
)
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validado_por")
    private Usuario validadoPor;

    @Column(name = "fecha_pago", nullable = false)
    private OffsetDateTime fechaPago;

    @Column(name = "fecha_validacion")
    private OffsetDateTime fechaValidacion;

    @DecimalMin("0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "monto",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "metodo_pago",
            nullable = false,
            length = 30
    )
    private MetodoPago metodoPago;

    @Size(max = 100)
    @Column(name = "referencia", length = 100)
    private String referencia;

    @NotBlank
    @Size(max = 100)
    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 100
    )
    private String idempotencyKey;

    @Size(max = 200)
    @Column(name = "observaciones", length = 200)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPago estado;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    public Pago() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaPago == null) {
            fechaPago = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = EstadoPago.PENDIENTE_VALIDACION;
        }
    }

    public Long getIdPago() {
        return idPago;
    }

    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public Usuario getValidadoPor() {
        return validadoPor;
    }

    public void setValidadoPor(Usuario validadoPor) {
        this.validadoPor = validadoPor;
    }

    public OffsetDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(OffsetDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public OffsetDateTime getFechaValidacion() {
        return fechaValidacion;
    }

    public void setFechaValidacion(OffsetDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public Integer getVersion() {
        return version;
    }
}