package com.credineyra.dto;

import com.credineyra.entity.enums.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class PagoRegistroDto {

    @NotNull(message = "Debe indicar el crédito")
    private Long idCredito;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(
            value = "1.00",
            message = "El monto debe ser mayor a cero"
    )
    private BigDecimal monto;

    @NotNull(message = "Debe seleccionar un método de pago")
    private MetodoPago metodoPago;

    @Size(max = 100)
    private String referencia;

    @Size(max = 200)
    private String observaciones;

    public PagoRegistroDto() {
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}