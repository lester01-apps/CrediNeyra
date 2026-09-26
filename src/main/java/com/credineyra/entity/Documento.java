package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoDocumento;
import com.credineyra.entity.enums.TipoDocumento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private Long idDocumento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credito")
    private Credito credito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pago")
    private Pago pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_garantia")
    private Garantia garantia;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_documento",
            nullable = false,
            length = 40
    )
    private TipoDocumento tipoDocumento;

    @Size(max = 50)
    @Column(
            name = "codigo_documento",
            unique = true,
            length = 50
    )
    private String codigoDocumento;

    @NotBlank
    @Size(max = 200)
    @Column(
            name = "nombre_archivo",
            nullable = false,
            length = 200
    )
    private String nombreArchivo;

    @NotBlank
    @Size(max = 500)
    @Column(
            name = "ruta_archivo",
            nullable = false,
            length = 500
    )
    private String rutaArchivo;

    @Column(name = "visible_cliente", nullable = false)
    private Boolean visibleCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDocumento estado;

    @Column(name = "fecha_subida", nullable = false)
    private OffsetDateTime fechaSubida;

    @Size(max = 200)
    @Column(name = "observaciones", length = 200)
    private String observaciones;

    public Documento() {
    }

    @PrePersist
    public void prePersist() {

        if (visibleCliente == null) {
            visibleCliente = false;
        }

        if (estado == null) {
            estado = EstadoDocumento.ACTIVO;
        }

        if (fechaSubida == null) {
            fechaSubida = OffsetDateTime.now();
        }
    }

    public Long getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(Long idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Credito getCredito() {
        return credito;
    }

    public void setCredito(Credito credito) {
        this.credito = credito;
    }

    public Pago getPago() {
        return pago;
    }

    public void setPago(Pago pago) {
        this.pago = pago;
    }

    public Garantia getGarantia() {
        return garantia;
    }

    public void setGarantia(Garantia garantia) {
        this.garantia = garantia;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getCodigoDocumento() {
        return codigoDocumento;
    }

    public void setCodigoDocumento(String codigoDocumento) {
        this.codigoDocumento = codigoDocumento;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public Boolean getVisibleCliente() {
        return visibleCliente;
    }

    public void setVisibleCliente(Boolean visibleCliente) {
        this.visibleCliente = visibleCliente;
    }

    public EstadoDocumento getEstado() {
        return estado;
    }

    public void setEstado(EstadoDocumento estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(OffsetDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}