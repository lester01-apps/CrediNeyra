package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.TipoOrigenCliente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClienteEdicionDto {

    /* =========================================================
       DATOS NO EDITABLES
       ========================================================= */

    private Long idCliente;

    private String codigoCliente;

    private String dni;

    private TipoOrigenCliente tipoOrigen;

    private EstadoCliente estado;


    /* =========================================================
       DATOS EDITABLES
       ========================================================= */

    @NotBlank(
            message = "Los nombres son obligatorios"
    )
    @Size(
            max = 100,
            message = "Los nombres no pueden superar 100 caracteres"
    )
    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$",
            message = "Los nombres contienen caracteres no permitidos"
    )
    private String nombres;


    @NotBlank(
            message = "Los apellidos son obligatorios"
    )
    @Size(
            max = 100,
            message = "Los apellidos no pueden superar 100 caracteres"
    )
    @Pattern(
            regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñÜü '-]+$",
            message = "Los apellidos contienen caracteres no permitidos"
    )
    private String apellidos;


    @Pattern(
            regexp = "^$|^[0-9]{9}$",
            message = "El teléfono debe contener 9 dígitos"
    )
    private String telefono;


    @Email(
            message = "Ingrese un correo electrónico válido"
    )
    @Size(
            max = 100,
            message = "El correo no puede superar 100 caracteres"
    )
    private String email;


    @Size(
            max = 200,
            message = "La dirección no puede superar 200 caracteres"
    )
    private String direccion;


    @Size(
            max = 200,
            message = "Las observaciones no pueden superar 200 caracteres"
    )
    private String observaciones;


    public ClienteEdicionDto() {
    }


    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(
            Long idCliente
    ) {
        this.idCliente = idCliente;
    }


    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(
            String codigoCliente
    ) {
        this.codigoCliente = codigoCliente;
    }


    public String getDni() {
        return dni;
    }

    public void setDni(
            String dni
    ) {
        this.dni = dni;
    }


    public TipoOrigenCliente getTipoOrigen() {
        return tipoOrigen;
    }

    public void setTipoOrigen(
            TipoOrigenCliente tipoOrigen
    ) {
        this.tipoOrigen = tipoOrigen;
    }


    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoCliente estado
    ) {
        this.estado = estado;
    }


    public String getNombres() {
        return nombres;
    }

    public void setNombres(
            String nombres
    ) {
        this.nombres = nombres;
    }


    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(
            String apellidos
    ) {
        this.apellidos = apellidos;
    }


    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(
            String telefono
    ) {
        this.telefono = telefono;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }


    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(
            String direccion
    ) {
        this.direccion = direccion;
    }


    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }
}