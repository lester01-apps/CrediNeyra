package com.credineyra.dto;

import com.credineyra.entity.enums.TipoOrigenCliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ClienteDto {

    private Long idCliente;

    @NotNull(message = "Debe seleccionar una persona")
    private Long idPersona;

    @NotBlank(message = "El código del cliente es obligatorio")
    @Size(max = 20)
    private String codigoCliente;

    @NotNull(message = "Debe indicar el origen del cliente")
    private TipoOrigenCliente tipoOrigen;

    @Size(max = 200)
    private String observaciones;

    public ClienteDto() {
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public TipoOrigenCliente getTipoOrigen() {
        return tipoOrigen;
    }

    public void setTipoOrigen(TipoOrigenCliente tipoOrigen) {
        this.tipoOrigen = tipoOrigen;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}