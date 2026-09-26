package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoRol;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @NotBlank(message = "El nombre del rol es obligatorio")
    @Size(
            max = 50,
            message = "El nombre del rol no puede superar los 50 caracteres"
    )
    @Column(
            name = "nombre",
            nullable = false,
            unique = true,
            length = 50
    )
    private String nombre;

    @Size(
            max = 100,
            message = "La descripción no puede superar los 100 caracteres"
    )
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRol estado;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "id_rol"),
            inverseJoinColumns = @JoinColumn(name = "id_permiso")
    )
    private Set<Permiso> permisos = new HashSet<>();

    public Rol() {
    }

    @PrePersist
    public void prePersist() {

        if (estado == null) {
            estado = EstadoRol.ACTIVO;
        }
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoRol getEstado() {
        return estado;
    }

    public void setEstado(EstadoRol estado) {
        this.estado = estado;
    }

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }
}