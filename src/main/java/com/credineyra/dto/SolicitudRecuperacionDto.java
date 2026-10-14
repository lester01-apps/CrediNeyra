package com.credineyra.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SolicitudRecuperacionDto {

    @NotBlank(
            message = "El correo es obligatorio"
    )
    @Email(
            message = "Ingrese un correo válido"
    )
    private String email;

    public SolicitudRecuperacionDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}