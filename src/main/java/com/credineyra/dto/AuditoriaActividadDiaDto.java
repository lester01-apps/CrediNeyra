package com.credineyra.dto;

import java.time.LocalDate;

public record AuditoriaActividadDiaDto(
        LocalDate fecha,
        long cantidad
) {
}
