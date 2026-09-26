package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoPlanDetalle;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

@Entity
@Table(
        name = "plan_regularizacion_detalle",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_cuota",
                        columnNames = {"id_plan", "id_cuota"}
                )
        }
)
public class PlanRegularizacionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_plan")
    private Long idDetallePlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_plan", nullable = false)
    private PlanRegularizacion plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuota", nullable = false)
    private Cuota cuota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @DecimalMin(value = "0.01")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "monto_capital_regularizado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal montoCapitalRegularizado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPlanDetalle estado;

    public PlanRegularizacionDetalle() {
    }

    @PrePersist
    public void prePersist() {
        if (estado == null) {
            estado = EstadoPlanDetalle.PENDIENTE;
        }
    }

    public Long getIdDetallePlan() {
        return idDetallePlan;
    }

    public void setIdDetallePlan(Long idDetallePlan) {
        this.idDetallePlan = idDetallePlan;
    }

    public PlanRegularizacion getPlan() {
        return plan;
    }

    public void setPlan(PlanRegularizacion plan) {
        this.plan = plan;
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

    public BigDecimal getMontoCapitalRegularizado() {
        return montoCapitalRegularizado;
    }

    public void setMontoCapitalRegularizado(
            BigDecimal montoCapitalRegularizado
    ) {
        this.montoCapitalRegularizado = montoCapitalRegularizado;
    }

    public EstadoPlanDetalle getEstado() {
        return estado;
    }

    public void setEstado(EstadoPlanDetalle estado) {
        this.estado = estado;
    }
}