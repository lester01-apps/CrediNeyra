package com.credineyra.dto;

import java.util.ArrayList;
import java.util.List;

public class PagoAprobacionDto {

    private List<PagoAplicacionDto> aplicaciones =
            new ArrayList<>();

    public PagoAprobacionDto() {
    }

    public List<PagoAplicacionDto> getAplicaciones() {
        return aplicaciones;
    }

    public void setAplicaciones(
            List<PagoAplicacionDto> aplicaciones
    ) {
        this.aplicaciones =
                aplicaciones;
    }
}