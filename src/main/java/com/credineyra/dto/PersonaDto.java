package com.credineyra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PersonaDto {

    private Long idPersona;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(
            regexp = "\\d{8}",
            message = "El DNI debe contener exactamente 8 números"
    )
    private String dni;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100)
    @Pattern(
            regexp = "^[-A-Za-zÁÉÍÓÚáéíóúÑñÜü ']+$",
            message = "Los nombres solo pueden contener letras"
    )
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100)
    @Pattern(
            regexp = "^[-A-Za-zÁÉÍÓÚáéíóúÑñÜü ']+$",
            message = "Los apellidos solo pueden contener letras"
    )
    private String apellidos;

    @Pattern(
            regexp = "^$|\\d{9}",
            message = "El teléfono debe contener 9 números"
    )
    private String telefono;

    @Email(message = "El correo electrónico no es válido")
    @Size(max = 150)
    private String email;

    @Size(max = 200)
    private String direccion;

    public PersonaDto() {
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
}