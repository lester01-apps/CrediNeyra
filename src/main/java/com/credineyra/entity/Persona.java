package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoPersona;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long idPersona;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(
            regexp = "^[0-9]{8}$",
            message = "El DNI debe contener exactamente 8 números"
    )
    @Column(
            name = "dni",
            nullable = false,
            unique = true,
            length = 8
    )
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(
            max = 100,
            message = "Los nombres no pueden superar los 100 caracteres"
    )
    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$",
            message = "Los nombres solo pueden contener letras"
    )
    @Column(
            name = "nombres",
            nullable = false,
            length = 100
    )
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(
            max = 100,
            message = "Los apellidos no pueden superar los 100 caracteres"
    )
    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$",
            message = "Los apellidos solo pueden contener letras"
    )
    @Column(
            name = "apellidos",
            nullable = false,
            length = 100
    )
    private String apellidos;

    @Pattern(
            regexp = "^$|^[0-9]{9}$",
            message = "El teléfono debe contener 9 números"
    )
    @Column(
            name = "telefono",
            length = 20
    )
    private String telefono;

    @Email(
            message = "El correo electrónico no tiene un formato válido"
    )
    @Size(
            max = 100,
            message = "El correo no puede superar los 100 caracteres"
    )
    @Column(
            name = "email",
            unique = true,
            length = 100
    )
    private String email;

    @Size(
            max = 200,
            message = "La dirección no puede superar los 200 caracteres"
    )
    @Column(
            name = "direccion",
            length = 200
    )
    private String direccion;

    @Column(
            name = "fecha_registro",
            nullable = false
    )
    private OffsetDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoPersona estado;

    public Persona() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaRegistro == null) {
            fechaRegistro = OffsetDateTime.now();
        }

        if (estado == null) {
            estado = EstadoPersona.ACTIVO;
        }
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public EstadoPersona getEstado() {
        return estado;
    }

    public void setEstado(EstadoPersona estado) {
        this.estado = estado;
    }
}