package com.credineyra.dto;

import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoCalculoCredito;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreditoRegistroDto {

    @NotNull(message = "Debe seleccionar un cliente")
    private Long idCliente;

    @Size(max = 20)
    private String codigoCredito;

    private Long idTasaOrigen;

    @NotNull(message = "Debe indicar el capital")
    @DecimalMin(
            value = "0.01",
            message = "El capital debe ser mayor que cero"
    )
    private BigDecimal capitalOriginal;

    @NotNull(message = "Debe seleccionar el tipo de interés")
    private TipoCalculoCredito tipoCalculo;

    @NotNull(message = "Debe indicar la tasa")
    @DecimalMin(
            value = "0.00",
            message = "La tasa no puede ser negativa"
    )
    private BigDecimal tasaInteres;

    /*
     * Indica cada cuánto se COBRA/APLICA
     * el porcentaje indicado.
     *
     * Valores permitidos para créditos:
     * SEMANAL
     * QUINCENAL
     * MENSUAL
     */
    @NotNull(message = "Debe indicar la periodicidad del interés")
    private Periodicidad periodicidadTasa;

    /*
     * Indica cada cuánto paga el cliente.
     *
     * DIARIA
     * SEMANAL
     * QUINCENAL
     * MENSUAL
     */
    @NotNull(message = "Debe indicar la forma de pago")
    private Periodicidad frecuenciaPago;

    @NotNull(message = "Debe indicar el plazo")
    @Min(
            value = 1,
            message = "El plazo debe ser mayor que cero"
    )
    private Integer plazoMeses;

    @NotNull(message = "Debe indicar la fecha de desembolso")
    private LocalDate fechaInicio;

    @NotNull(message = "Debe indicar la primera fecha de pago")
    private LocalDate fechaPrimerPago;

    private Boolean permitePagoSoloInteres = false;

    @Size(max = 200)
    private String observaciones;

    /*
     * GARANTÍA
     *
     * >= S/500 -> obligatoria
     * <  S/500 -> opcional
     */
    @Size(max = 50)
    private String tipoGarantia;

    @Size(max = 200)
    private String descripcionGarantia;

    private BigDecimal valorEstimadoGarantia;

    @Size(max = 200)
    private String observacionesGarantia;

    /*
     * Opcional.
     * Nos servirá posteriormente para el contrato.
     */
    @Size(max = 150)
    private String nombreGarante;

    @Size(max = 20)
    private String documentoGarante;

    @Size(max = 200)
    private String domicilioGarante;

    public CreditoRegistroDto() {
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public String getCodigoCredito() {
        return codigoCredito;
    }

    public void setCodigoCredito(String codigoCredito) {
        this.codigoCredito = codigoCredito;
    }

    public Long getIdTasaOrigen() {
        return idTasaOrigen;
    }

    public void setIdTasaOrigen(Long idTasaOrigen) {
        this.idTasaOrigen = idTasaOrigen;
    }

    public BigDecimal getCapitalOriginal() {
        return capitalOriginal;
    }

    public void setCapitalOriginal(
            BigDecimal capitalOriginal
    ) {
        this.capitalOriginal = capitalOriginal;
    }

    public TipoCalculoCredito getTipoCalculo() {
        return tipoCalculo;
    }

    public void setTipoCalculo(
            TipoCalculoCredito tipoCalculo
    ) {
        this.tipoCalculo = tipoCalculo;
    }

    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }

    public void setTasaInteres(
            BigDecimal tasaInteres
    ) {
        this.tasaInteres = tasaInteres;
    }

    public Periodicidad getPeriodicidadTasa() {
        return periodicidadTasa;
    }

    public void setPeriodicidadTasa(
            Periodicidad periodicidadTasa
    ) {
        this.periodicidadTasa = periodicidadTasa;
    }

    public Periodicidad getFrecuenciaPago() {
        return frecuenciaPago;
    }

    public void setFrecuenciaPago(
            Periodicidad frecuenciaPago
    ) {
        this.frecuenciaPago = frecuenciaPago;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(
            Integer plazoMeses
    ) {
        this.plazoMeses = plazoMeses;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(
            LocalDate fechaInicio
    ) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaPrimerPago() {
        return fechaPrimerPago;
    }

    public void setFechaPrimerPago(
            LocalDate fechaPrimerPago
    ) {
        this.fechaPrimerPago = fechaPrimerPago;
    }

    public Boolean getPermitePagoSoloInteres() {
        return permitePagoSoloInteres;
    }

    public void setPermitePagoSoloInteres(
            Boolean permitePagoSoloInteres
    ) {
        this.permitePagoSoloInteres =
                permitePagoSoloInteres;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {
        this.observaciones = observaciones;
    }

    public String getTipoGarantia() {
        return tipoGarantia;
    }

    public void setTipoGarantia(
            String tipoGarantia
    ) {
        this.tipoGarantia = tipoGarantia;
    }

    public String getDescripcionGarantia() {
        return descripcionGarantia;
    }

    public void setDescripcionGarantia(
            String descripcionGarantia
    ) {
        this.descripcionGarantia =
                descripcionGarantia;
    }

    public BigDecimal getValorEstimadoGarantia() {
        return valorEstimadoGarantia;
    }

    public void setValorEstimadoGarantia(
            BigDecimal valorEstimadoGarantia
    ) {
        this.valorEstimadoGarantia =
                valorEstimadoGarantia;
    }

    public String getObservacionesGarantia() {
        return observacionesGarantia;
    }

    public void setObservacionesGarantia(
            String observacionesGarantia
    ) {
        this.observacionesGarantia =
                observacionesGarantia;
    }

    public String getNombreGarante() {
        return nombreGarante;
    }

    public void setNombreGarante(
            String nombreGarante
    ) {
        this.nombreGarante = nombreGarante;
    }

    public String getDocumentoGarante() {
        return documentoGarante;
    }

    public void setDocumentoGarante(
            String documentoGarante
    ) {
        this.documentoGarante =
                documentoGarante;
    }

    public String getDomicilioGarante() {
        return domicilioGarante;
    }

    public void setDomicilioGarante(
            String domicilioGarante
    ) {
        this.domicilioGarante =
                domicilioGarante;
    }
}