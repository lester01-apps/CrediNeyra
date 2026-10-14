package com.credineyra.dto;

import com.credineyra.entity.enums.CanalNotificacion;
import com.credineyra.entity.enums.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public class NotificacionDto {

    @NotNull(message = "Debe indicar el cliente")
    private Long idCliente;

    private Long idCredito;

    private Long idCuota;

    @NotNull(message = "Debe indicar el tipo de notificación")
    private TipoNotificacion tipo;

    @NotNull(message = "Debe seleccionar el canal")
    private CanalNotificacion canal;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100)
    private String titulo;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 500)
    private String mensaje;

    private OffsetDateTime fechaProgramada;

    public NotificacionDto() {
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Long getIdCredito() {
        return idCredito;
    }

    public void setIdCredito(Long idCredito) {
        this.idCredito = idCredito;
    }

    public Long getIdCuota() {
        return idCuota;
    }

    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacion canal) {
        this.canal = canal;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public OffsetDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(
            OffsetDateTime fechaProgramada
    ) {
        this.fechaProgramada = fechaProgramada;
    }
}