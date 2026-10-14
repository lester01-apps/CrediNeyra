package com.credineyra.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlanRegularizacionRegistroDto {

    @NotNull(message = "El crédito es obligatorio")
    private Long idCredito;

    @NotNull(message = "La cantidad de abonos es obligatoria")
    @Min(
            value = 1,
            message = "Debe existir al menos un abono"
    )
    private Integer cantidadAbonos;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(
            max = 200,
            message = "El motivo no puede superar 200 caracteres"
    )
    private String motivo;

    public PlanRegularizacionRegistroDto() {
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Integer getCantidadAbonos() {
        return cantidadAbonos;
    }

    public void setCantidadAbonos(
            Integer cantidadAbonos
    ) {
        this.cantidadAbonos = cantidadAbonos;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}