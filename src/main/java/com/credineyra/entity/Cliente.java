package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.entity.enums.TipoOrigenCliente;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_persona",
            nullable = false,
            unique = true
    )
    private Persona persona;

    @NotBlank(message = "El código del cliente es obligatorio")
    @Size(
            max = 20,
            message = "El código del cliente no puede superar los 20 caracteres"
    )
    @Column(
            name = "codigo_cliente",
            nullable = false,
            unique = true,
            length = 20
    )
    private String codigoCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_origen", nullable = false, length = 20)
    private TipoOrigenCliente tipoOrigen;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCliente estado;

    @Size(
            max = 200,
            message = "Las observaciones no pueden superar los 200 caracteres"
    )
    @Column(name = "observaciones", length = 200)
    private String observaciones;

    public Cliente() {
    }

    @PrePersist
    public void prePersist() {

        if (fechaRegistro == null) {
            fechaRegistro = OffsetDateTime.now();
        }

        if (tipoOrigen == null) {
            tipoOrigen = TipoOrigenCliente.NUEVO;
        }

        if (estado == null) {
            estado = EstadoCliente.ACTIVO;
        }
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
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

    public OffsetDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(OffsetDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public EstadoCliente getEstado() {
        return estado;
    }

    public void setEstado(EstadoCliente estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}