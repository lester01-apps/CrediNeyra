package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoPlanRegularizacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "plan_regularizacion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_credito",
                        columnNames = {"id_plan", "id_credito"}
                )
        }
)
public class PlanRegularizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plan")
    private Long idPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private Usuario usuarioCreador;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_pendiente_origen",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalPendienteOrigen;

    @Min(1)
    @Column(name = "cantidad_abonos", nullable = false)
    private Integer cantidadAbonos;

    @NotBlank
    @Size(max = 200)
    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPlanRegularizacion estado;

    public PlanRegularizacion() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = EstadoPlanRegularizacion.ACTIVO;
        }
    }

    public Long getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(Long idPlan) {
        this.idPlan = idPlan;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public BigDecimal getCapitalPendienteOrigen() {
        return capitalPendienteOrigen;
    }

    public void setCapitalPendienteOrigen(
            BigDecimal capitalPendienteOrigen
    ) {
        this.capitalPendienteOrigen = capitalPendienteOrigen;
    }

    public Integer getCantidadAbonos() {
        return cantidadAbonos;
    }

    public void setCantidadAbonos(Integer cantidadAbonos) {
        this.cantidadAbonos = cantidadAbonos;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoPlanRegularizacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanRegularizacion estado) {
        this.estado = estado;
    }
}