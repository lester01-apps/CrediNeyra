package com.credineyra.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "usuario_rol")
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id = new UsuarioRolId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "fecha_asignacion", nullable = false)
    private OffsetDateTime fechaAsignacion;

    public UsuarioRol() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaAsignacion == null) {
            fechaAsignacion = OffsetDateTime.now();
        }
    }

    public UsuarioRolId getId() {
        return id;
    }

    public void setId(UsuarioRolId id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public OffsetDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(OffsetDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    @Embeddable
    public static class UsuarioRolId implements Serializable {

        @Column(name = "id_usuario")
        private Long idUsuario;

        @Column(name = "id_rol")
        private Long idRol;

        public UsuarioRolId() {
        }

        public UsuarioRolId(Long idUsuario, Long idRol) {
            this.idUsuario = idUsuario;
            this.idRol = idRol;
        }

        public Long getIdUsuario() {
            return idUsuario;
        }

        public void setIdUsuario(Long idUsuario) {
            this.idUsuario = idUsuario;
        }

        public Long getIdRol() {
            return idRol;
        }

        public void setIdRol(Long idRol) {
            this.idRol = idRol;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            if (!(o instanceof UsuarioRolId that)) {
                return false;
            }

            return Objects.equals(idUsuario, that.idUsuario)
                    && Objects.equals(idRol, that.idRol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idUsuario, idRol);
        }
    }
}