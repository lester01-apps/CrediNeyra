package com.credineyra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "permiso",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_permiso_modulo_nombre",
                        columnNames = {"modulo", "nombre"}
                )
        }
)
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @NotBlank(message = "El nombre del permiso es obligatorio")
    @Size(
            max = 50,
            message = "El nombre del permiso no puede superar los 50 caracteres"
    )
    @Column(
            name = "nombre",
            nullable = false,
            length = 50
    )
    private String nombre;

    @Size(
            max = 50,
            message = "El módulo no puede superar los 50 caracteres"
    )
    @Column(name = "modulo", length = 50)
    private String modulo;

    @Size(
            max = 100,
            message = "La descripción no puede superar los 100 caracteres"
    )
    @Column(name = "descripcion", length = 100)
    private String descripcion;

    public Permiso() {
    }

    public Long getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(Long idPermiso) {
        this.idPermiso = idPermiso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}