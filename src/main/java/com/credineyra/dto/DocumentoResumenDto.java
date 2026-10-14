package com.credineyra.dto;

import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;

import java.time.OffsetDateTime;

public class DocumentoResumenDto {

    private Long idDocumento;
    private String codigoDocumento;
    private TipoDocumento tipoDocumento;
    private String nombreArchivo;
    private EstadoDocumento estado;
    private OffsetDateTime fechaSubida;
    private Boolean visibleCliente;
    private String observaciones;

    public DocumentoResumenDto() {
    }

    public DocumentoResumenDto(
            Long idDocumento,
            String codigoDocumento,
            TipoDocumento tipoDocumento,
            String nombreArchivo,
            EstadoDocumento estado,
            OffsetDateTime fechaSubida,
            Boolean visibleCliente,
            String observaciones
    ) {
        this.idDocumento = idDocumento;
        this.codigoDocumento = codigoDocumento;
        this.tipoDocumento = tipoDocumento;
        this.nombreArchivo = nombreArchivo;
        this.estado = estado;
        this.fechaSubida = fechaSubida;
        this.visibleCliente = visibleCliente;
        this.observaciones = observaciones;
    }

    public Long getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(Long idDocumento) {
        this.idDocumento = idDocumento;
    }

    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public void setCodigoDocumento(String codigoDocumento) {
        this.codigoDocumento = codigoDocumento;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(
            TipoDocumento tipoDocumento
    ) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(
            String nombreArchivo
    ) {
        this.nombreArchivo = nombreArchivo;
    }

    public EstadoDocumento getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoDocumento estado
    ) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(
            OffsetDateTime fechaSubida
    ) {
        this.fechaSubida = fechaSubida;
    }

    public Boolean getVisibleCliente() {
        return visibleCliente;
    }

    public void setVisibleCliente(
            Boolean visibleCliente
    ) {
        this.visibleCliente = visibleCliente;
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