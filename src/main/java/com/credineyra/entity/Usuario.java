package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_persona",
            nullable = false,
            unique = true
    )
    private Persona persona;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(
            min = 3,
            max = 50,
            message = "El nombre de usuario debe tener entre 3 y 50 caracteres"
    )
    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 50
    )
    private String username;

    @NotBlank(message = "La contraseña cifrada es obligatoria")
    @Size(
            max = 255,
            message = "La contraseña cifrada no puede superar los 255 caracteres"
    )
    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUsuario estado;

    @Column(
            name = "cambio_password_obligatorio",
            nullable = false
    )
    private Boolean cambioPasswordObligatorio;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    public Usuario() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaCreacion == null) {
            fechaCreacion = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = EstadoUsuario.ACTIVA;
        }

        if (cambioPasswordObligatorio == null) {
            cambioPasswordObligatorio = true;
        }
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public Boolean getCambioPasswordObligatorio() {
        return cambioPasswordObligatorio;
    }

    public void setCambioPasswordObligatorio(Boolean cambioPasswordObligatorio) {
        this.cambioPasswordObligatorio = cambioPasswordObligatorio;
    }

    public OffsetDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(OffsetDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(OffsetDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Integer getVersion() {
        return version;
    }
}