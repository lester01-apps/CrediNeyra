package com.credineyra.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

@Entity
@Table(name = "recuperacion_password")
public class RecuperacionPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recuperacion")
    private Long idRecuperacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "id_usuario",
            nullable = false
    )
    private Usuario usuario;

    @NotBlank(message = "El token de recuperación es obligatorio")
    @Size(
            max = 255,
            message = "El token no puede superar los 255 caracteres"
    )
    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 255
    )
    private String tokenHash;

    @Column(name = "expira_en", nullable = false)
    private OffsetDateTime expiraEn;

    @Column(name = "usado_en")
    private OffsetDateTime usadoEn;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    public RecuperacionPassword() {
    }

    @PrePersist
    public void prePersist() {

        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    public Long getIdRecuperacion() {
        return idRecuperacion;
    }

    public void setIdRecuperacion(Long idRecuperacion) {
        this.idRecuperacion = idRecuperacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public OffsetDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(OffsetDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public OffsetDateTime getUsadoEn() {
        return usadoEn;
    }

    public void setUsadoEn(OffsetDateTime usadoEn) {
        this.usadoEn = usadoEn;
    }

    public OffsetDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(OffsetDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}