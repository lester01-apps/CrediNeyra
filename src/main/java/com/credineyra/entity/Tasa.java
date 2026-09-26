package com.credineyra.entity;

import com.credineyra.entity.enums.EstadoTasa;
import com.credineyra.entity.enums.Periodicidad;
import com.credineyra.entity.enums.TipoTasa;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "tasa",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tasa_codigo_version",
                        columnNames = {"codigo", "version"}
                )
        }
)
public class Tasa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tasa")
    private Long idTasa;

    @NotBlank(message = "El código de la tasa es obligatorio")
    @Size(
            max = 30,
            message = "El código no puede superar los 30 caracteres"
    )
    @Column(
            name = "codigo",
            nullable = false,
            length = 30
    )
    private String codigo;

    @Min(
            value = 1,
            message = "La versión debe ser mayor a 0"
    )
    @Column(
            name = "version",
            nullable = false
    )
    private Integer version;

    @NotBlank(message = "El nombre de la tasa es obligatorio")
    @Size(
            max = 100,
            message = "El nombre no puede superar los 100 caracteres"
    )
    @Column(
            name = "nombre",
            nullable = false,
            length = 100
    )
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo",
            nullable = false,
            length = 20
    )
    private TipoTasa tipo;

    @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "La tasa no puede ser negativa"
    )
    @Digits(
            integer = 3,
            fraction = 2,
            message = "La tasa debe tener como máximo 3 enteros y 2 decimales"
    )
    @Column(
            name = "valor",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "periodicidad",
            nullable = false,
            length = 30
    )
    private Periodicidad periodicidad;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "estado",
            nullable = false,
            length = 20
    )
    private EstadoTasa estado;

    @Column(
            name = "vigente_desde",
            nullable = false
    )
    private OffsetDateTime vigenteDesde;

    @Column(name = "vigente_hasta")
    private OffsetDateTime vigenteHasta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_usuario_creador",
            nullable = false
    )
    private Usuario usuarioCreador;

    @NotBlank(message = "El motivo de la tasa es obligatorio")
    @Size(
            max = 200,
            message = "El motivo no puede superar los 200 caracteres"
    )
    @Column(
            name = "motivo",
            nullable = false,
            length = 200
    )
    private String motivo;

    public Tasa() {
    }

    @PrePersist
    public void prePersist() {

        if (estado == null) {
            estado = EstadoTasa.ACTIVA;
        }

        if (vigenteDesde == null) {
            vigenteDesde = OffsetDateTime.now();
        }
    }

    public Long getIdTasa() {
        return idTasa;
    }

    public void setIdTasa(Long idTasa) {
        this.idTasa = idTasa;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoTasa getTipo() {
        return tipo;
    }

    public void setTipo(TipoTasa tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Periodicidad getPeriodicidad() {
        return periodicidad;
    }

    public void setPeriodicidad(Periodicidad periodicidad) {
        this.periodicidad = periodicidad;
    }

    public EstadoTasa getEstado() {
        return estado;
    }

    public void setEstado(EstadoTasa estado) {
        this.estado = estado;
    }

    public OffsetDateTime getVigenteDesde() {
        return vigenteDesde;
    }

    public void setVigenteDesde(OffsetDateTime vigenteDesde) {
        this.vigenteDesde = vigenteDesde;
    }

    public OffsetDateTime getVigenteHasta() {
        return vigenteHasta;
    }

    public void setVigenteHasta(OffsetDateTime vigenteHasta) {
        this.vigenteHasta = vigenteHasta;
    }

    public Usuario getUsuarioCreador() {
        return usuarioCreador;
    }

    public void setUsuarioCreador(Usuario usuarioCreador) {
        this.usuarioCreador = usuarioCreador;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}