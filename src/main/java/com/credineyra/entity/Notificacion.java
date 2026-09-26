package com.credineyra.entity;

import com.credineyra.entity.enums.CanalNotificacion;
import com.credineyra.entity.enums.EstadoNotificacion;
import com.credineyra.entity.enums.TipoNotificacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credito")
    private Credito credito;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuota")
    private Cuota cuota;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoNotificacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 20)
    private CanalNotificacion canal;

    @NotBlank
    @Size(max = 100)
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @NotBlank
    @Size(max = 500)
    @Column(name = "mensaje", nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoNotificacion estado;

    @Column(name = "leida", nullable = false)
    private Boolean leida;

    @Column(name = "fecha_programada", nullable = false)
    private OffsetDateTime fechaProgramada;

    @Column(name = "fecha_enviada")
    private OffsetDateTime fechaEnviada;

    @Column(name = "fecha_leida")
    private OffsetDateTime fechaLeida;

    @Size(max = 120)
    @Column(
            name = "clave_evento",
            unique = true,
            length = 120
    )
    private String claveEvento;

    public Notificacion() {
    }

    @PrePersist
    public void prePersist() {

        if (estado == null) {
            estado = EstadoNotificacion.PENDIENTE;
        }

        if (leida == null) {
            leida = false;
        }

        if (fechaProgramada == null) {
            fechaProgramada = OffsetDateTime.now();
        }
    }

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
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

    public Cuota getCuota() {
        return cuota;
    }

    public void setCuota(Cuota cuota) {
        this.cuota = cuota;
    }

    public Usuario getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
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

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoNotificacion estado) {
        this.estado = estado;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    public OffsetDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(OffsetDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public OffsetDateTime getFechaEnviada() {
        return fechaEnviada;
    }

    public void setFechaEnviada(OffsetDateTime fechaEnviada) {
        this.fechaEnviada = fechaEnviada;
    }

    public OffsetDateTime getFechaLeida() {
        return fechaLeida;
    }

    public void setFechaLeida(OffsetDateTime fechaLeida) {
        this.fechaLeida = fechaLeida;
    }

    public String getClaveEvento() {
        return claveEvento;
    }

    public void setClaveEvento(String claveEvento) {
        this.claveEvento = claveEvento;
    }
}