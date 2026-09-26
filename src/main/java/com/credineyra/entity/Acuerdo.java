package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoAcuerdo;
import com.credineyra.entity.enums.TipoAcuerdo;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "acuerdo")
public class Acuerdo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acuerdo")
    private Long idAcuerdo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuota")
    private Cuota cuota;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_creador", nullable = false)
    private Usuario usuarioCreador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_acuerdo", nullable = false, length = 50)
    private TipoAcuerdo tipoAcuerdo;

    @Column(name = "fecha_original")
    private LocalDate fechaOriginal;

    @Column(name = "fecha_acordada")
    private LocalDate fechaAcordada;

    @Min(1)
    @Column(name = "dias_aplazamiento")
    private Integer diasAplazamiento;

    @NotBlank
    @Size(max = 200)
    @Column(name = "motivo", nullable = false, length = 200)
    private String motivo;

    @Size(max = 200)
    @Column(name = "observaciones", length = 200)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAcuerdo estado;

    public Acuerdo() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = EstadoAcuerdo.ACTIVO;
        }
    }

    public Long getIdAcuerdo() {
        return idAcuerdo;
    }

    public void setIdAcuerdo(Long idAcuerdo) {
        this.idAcuerdo = idAcuerdo;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public Cuota getCuota() {
        return cuota;
    }

    public void setCuota(Cuota cuota) {
        this.cuota = cuota;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public TipoAcuerdo getTipoAcuerdo() {
        return tipoAcuerdo;
    }

    public void setTipoAcuerdo(TipoAcuerdo tipoAcuerdo) {
        this.tipoAcuerdo = tipoAcuerdo;
    }

    public LocalDate getFechaOriginal() {
        return fechaOriginal;
    }

    public void setFechaOriginal(LocalDate fechaOriginal) {
        this.fechaOriginal = fechaOriginal;
    }

    public LocalDate getFechaAcordada() {
        return fechaAcordada;
    }

    public void setFechaAcordada(LocalDate fechaAcordada) {
        this.fechaAcordada = fechaAcordada;
    }

    public Integer getDiasAplazamiento() {
        return diasAplazamiento;
    }

    public void setDiasAplazamiento(Integer diasAplazamiento) {
        this.diasAplazamiento = diasAplazamiento;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public EstadoAcuerdo getEstado() {
        return estado;
    }

    public void setEstado(EstadoAcuerdo estado) {
        this.estado = estado;
    }
}