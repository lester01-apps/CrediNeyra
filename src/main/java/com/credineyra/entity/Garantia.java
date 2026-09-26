package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoGarantia;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "garantia",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_garantia_credito",
                        columnNames = {"id_garantia", "id_credito"}
                )
        }
)
public class Garantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_garantia")
    private Long idGarantia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @NotBlank
    @Size(max = 50)
    @Column(
            name = "tipo_garantia",
            nullable = false,
            length = 50
    )
    private String tipoGarantia;

    @Size(max = 200)
    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "valor_estimado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorEstimado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoGarantia estado;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Size(max = 200)
    @Column(name = "observaciones", length = 200)
    private String observaciones;

    public Garantia() {
    }

    @PrePersist
    public void prePersist() {

        if (estado == null) {
            estado = EstadoGarantia.ACTIVA;
        }

        if (fechaRegistro == null) {
            fechaRegistro = OffsetDateTime.now();
        }
    }

    public Long getIdGarantia() {
        return idGarantia;
    }

    public void setIdGarantia(Long idGarantia) {
        this.idGarantia = idGarantia;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public String getTipoGarantia() {
        return tipoGarantia;
    }

    public void setTipoGarantia(String tipoGarantia) {
        this.tipoGarantia = tipoGarantia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getValorEstimado() {
        return valorEstimado;
    }

    public void setValorEstimado(BigDecimal valorEstimado) {
        this.valorEstimado = valorEstimado;
    }

    public EstadoGarantia getEstado() {
        return estado;
    }

    public void setEstado(EstadoGarantia estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}