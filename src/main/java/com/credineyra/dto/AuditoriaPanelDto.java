package com.credineyra.dto;

import java.util.List;

public record AuditoriaPanelDto(
        List<AuditoriaFilaDto> filas,
        List<AuditoriaFilaDto> todasLasFilas,
        long eventosRegistrados,
        long usuariosActivos,
        long solicitudesPendientes,
        long accionesCriticas,
        List<String> acciones,
        List<String> modulos,
        List<AuditoriaFilaDto> alertas,
        List<AuditoriaActividadDiaDto> actividadDiaria,
        List<AuditoriaConteoDto> accionesFrecuentes,
        long maximoActividad,
        long maximoAcciones
) {
}
