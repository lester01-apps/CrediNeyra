package com.credineyra.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PagoClienteRegistroDto {

    @NotNull(message = "Debe indicar el crédito")
    private Long idCredito;

    @NotNull(message = "El monto pagado es obligatorio")
    @DecimalMin(
            value = "1.00",
            message = "El monto debe ser mayor a cero"
    )
    private BigDecimal monto;

    @Size(max = 20, message = "El tipo de pago no es válido")
    private String tipoPago;

    @Size(
            max = 100,
            message = "La referencia no puede superar 100 caracteres"
    )
    private String referencia;

    @Size(
            max = 200,
            message = "La observación no puede superar 200 caracteres"
    )
    private String observaciones;


    public PagoClienteRegistroDto() {
    }


    public Long getIdCredito() {
        return idCredito;
    }


    public void setIdCredito(
            Long idCredito
    ) {
        this.idCredito = idCredito;
    }


    public BigDecimal getMonto() {
        return monto;
    }


    public void setMonto(
            BigDecimal monto
    ) {
        this.monto = monto;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }


    public String getReferencia() {
        return referencia;
    }


    public void setReferencia(
            String referencia
    ) {
        this.referencia = referencia;
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
