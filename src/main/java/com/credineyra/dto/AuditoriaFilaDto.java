package com.credineyra.dto;

import java.time.OffsetDateTime;

public record AuditoriaFilaDto(
        OffsetDateTime fechaHora,
        Long idUsuario,
        String usuario,
        String accion,
        String modulo,
        String detalle,
        String resultado
) {
}
