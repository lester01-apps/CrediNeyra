package com.credineyra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteContactoDto {

    @NotBlank(
            message = "El asunto es obligatorio"
    )
    @Size(
            max = 160,
            message = "El asunto no puede superar 160 caracteres"
    )
    private String asunto;


    @NotBlank(
            message = "El mensaje es obligatorio"
    )
    @Size(
            max = 4000,
            message = "El mensaje no puede superar 4000 caracteres"
    )
    private String mensaje;


    public ClienteContactoDto() {
    }


    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(
            String asunto
    ) {
        this.asunto = asunto;
    }


    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(
            String mensaje
    ) {
        this.mensaje = mensaje;
    }
}