package com.credineyra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RestablecerPasswordDto {

    @NotBlank
    private String token;

    @NotBlank(
            message = "La contraseña es obligatoria"
    )
    @Size(
            min = 8,
            max = 100,
            message = "La contraseña debe tener entre 8 y 100 caracteres"
    )
    private String password;

    @NotBlank(
            message = "Debe confirmar la contraseña"
    )
    private String confirmarPassword;

    public RestablecerPasswordDto() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(
            String password
    ) {
        this.password = password;
    }

    public String getConfirmarPassword() {
        return confirmarPassword;
    }

    public void setConfirmarPassword(
            String confirmarPassword
    ) {
        this.confirmarPassword =
                confirmarPassword;
    }
}