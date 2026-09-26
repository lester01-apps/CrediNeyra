package com.credineyra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "traspaso_cliente",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_traspaso_cliente",
                        columnNames = "id_cliente"
                )
        }
)
public class TraspasoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_traspaso")
    private Long idTraspaso;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_cliente",
            nullable = false,
            unique = true
    )
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "capital_pendiente_inicial",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal capitalPendienteInicial;

    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(
            name = "interes_pendiente_inicial",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal interesPendienteInicial;

    @Column(name = "fecha_corte_origen")
    private LocalDate fechaCorteOrigen;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    @Size(max = 200)
    @Column(name = "observacion_motivo", length = 200)
    private String observacionMotivo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_iniciales", columnDefinition = "jsonb")
    private String datosIniciales;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "obligaciones_incorporadas",
            columnDefinition = "jsonb"
    )
    private String obligacionesIncorporadas;

    public TraspasoCliente() {
    }

    @PrePersist
    public void prePersist() {

        if (capitalPendienteInicial == null) {
            capitalPendienteInicial = BigDecimal.ZERO;
        }

        if (interesPendienteInicial == null) {
            interesPendienteInicial = BigDecimal.ZERO;
        }

        if (fechaHora == null) {
            fechaHora = OffsetDateTime.now();
        }
    }

    public Long getIdTraspaso() {
        return idTraspaso;
    }

    public void setIdTraspaso(Long idTraspaso) {
        this.idTraspaso = idTraspaso;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Usuario getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Usuario registradoPor) {
        this.registradoPor = registradoPor;
    }

    public BigDecimal getCapitalPendienteInicial() {
        return capitalPendienteInicial;
    }

    public void setCapitalPendienteInicial(BigDecimal capitalPendienteInicial) {
        this.capitalPendienteInicial = capitalPendienteInicial;
    }

    public BigDecimal getInteresPendienteInicial() {
        return interesPendienteInicial;
    }

    public void setInteresPendienteInicial(BigDecimal interesPendienteInicial) {
        this.interesPendienteInicial = interesPendienteInicial;
    }

    public LocalDate getFechaCorteOrigen() {
        return fechaCorteOrigen;
    }

    public void setFechaCorteOrigen(LocalDate fechaCorteOrigen) {
        this.fechaCorteOrigen = fechaCorteOrigen;
    }

    public OffsetDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(OffsetDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getObservacionMotivo() {
        return observacionMotivo;
    }

    public void setObservacionMotivo(String observacionMotivo) {
        this.observacionMotivo = observacionMotivo;
    }

    public String getDatosIniciales() {
        return datosIniciales;
    }

    public void setDatosIniciales(String datosIniciales) {
        this.datosIniciales = datosIniciales;
    }

    public String getObligacionesIncorporadas() {
        return obligacionesIncorporadas;
    }

    public void setObligacionesIncorporadas(
            String obligacionesIncorporadas
    ) {
        this.obligacionesIncorporadas = obligacionesIncorporadas;
    }
}