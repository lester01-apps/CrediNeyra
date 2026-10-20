package com.credineyra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Campos que un administrador puede modificar sobre su propia ficha. */
public class AdminPerfilEdicionDto {

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden superar 100 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$", message = "Los nombres contienen caracteres no permitidos")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden superar 100 caracteres")
    @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$", message = "Los apellidos contienen caracteres no permitidos")
    private String apellidos;

    @Pattern(regexp = "^$|^[0-9]{9}$", message = "El teléfono debe contener 9 dígitos")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede superar 200 caracteres")
    private String direccion;

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
