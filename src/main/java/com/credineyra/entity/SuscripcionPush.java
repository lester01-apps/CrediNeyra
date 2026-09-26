package com.credineyra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "suscripcion_push")
public class SuscripcionPush {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion")
    private Long idSuscripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @NotBlank
    @Size(max = 500)
    @Column(
            name = "token_fcm",
            nullable = false,
            unique = true,
            length = 500
    )
    private String tokenFcm;

    @Size(max = 100)
    @Column(name = "dispositivo", length = 100)
    private String dispositivo;

    @Size(max = 100)
    @Column(name = "navegador", length = 100)
    private String navegador;

    @Size(max = 100)
    @Column(name = "sistema_operativo", length = 100)
    private String sistemaOperativo;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public SuscripcionPush() {
    }

    @PrePersist
    public void prePersist() {

        OffsetDateTime ahora = OffsetDateTime.now();

        if (activo == null) {
            activo = true;
        }

        if (fechaRegistro == null) {
            fechaRegistro = ahora;
        }

        if (fechaActualizacion == null) {
            fechaActualizacion = ahora;
        }
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = OffsetDateTime.now();
    }

    public Long getIdSuscripcion() {
        return idSuscripcion;
    }

    public void setIdSuscripcion(Long idSuscripcion) {
        this.idSuscripcion = idSuscripcion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTokenFcm() {
        return tokenFcm;
    }

    public void setTokenFcm(String tokenFcm) {
        this.tokenFcm = tokenFcm;
    }

    public String getDispositivo() {
        return dispositivo;
    }

    public void setDispositivo(String dispositivo) {
        this.dispositivo = dispositivo;
    }

    public String getNavegador() {
        return navegador;
    }

    public void setNavegador(String navegador) {
        this.navegador = navegador;
    }

    public String getSistemaOperativo() {
        return sistemaOperativo;
    }

    public void setSistemaOperativo(String sistemaOperativo) {
        this.sistemaOperativo = sistemaOperativo;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(OffsetDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}