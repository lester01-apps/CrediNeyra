package com.credineyra.service;

import com.credineyra.dto.AuditoriaFilaDto;
import com.credineyra.dto.AuditoriaActividadDiaDto;
import com.credineyra.dto.AuditoriaConteoDto;
import com.credineyra.dto.AuditoriaPanelDto;
import com.credineyra.entity.Auditoria;
import com.credineyra.entity.Usuario;
import com.credineyra.entity.enums.EstadoUsuario;
import com.credineyra.repository.AuditoriaRepository;
import com.credineyra.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuditoriaPanelService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaPanelService(AuditoriaRepository auditoriaRepository,
                                 UsuarioRepository usuarioRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public AuditoriaPanelDto obtener(LocalDate desde, LocalDate hasta, Long idUsuario,
                                     String accion, String modulo, String resultado) {
        List<AuditoriaFilaDto> todas = auditoriaRepository.findAllByOrderByFechaHoraDesc().stream()
                .map(this::fila)
                .toList();
        List<AuditoriaFilaDto> filtradas = todas.stream()
                .filter(fila -> desde == null || !fila.fechaHora().toLocalDate().isBefore(desde))
                .filter(fila -> hasta == null || !fila.fechaHora().toLocalDate().isAfter(hasta))
                .filter(fila -> idUsuario == null || idUsuario.equals(fila.idUsuario()))
                .filter(fila -> vacio(accion) || accion.equals(fila.accion()))
                .filter(fila -> vacio(modulo) || modulo.equals(fila.modulo()))
                .filter(fila -> vacio(resultado) || resultado.equals(fila.resultado()))
                .toList();
        List<AuditoriaFilaDto> alertas = filtradas.stream()
                .filter(fila -> !"EXITOSO".equals(fila.resultado())).limit(5).toList();
        List<AuditoriaActividadDiaDto> actividad = filtradas.stream()
                .collect(Collectors.groupingBy(fila -> fila.fechaHora().toLocalDate(), Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(item -> new AuditoriaActividadDiaDto(item.getKey(), item.getValue())).toList();
        List<AuditoriaConteoDto> frecuentes = filtradas.stream()
                .collect(Collectors.groupingBy(AuditoriaFilaDto::accion, Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5).map(item -> new AuditoriaConteoDto(item.getKey(), item.getValue())).toList();

        return new AuditoriaPanelDto(
                filtradas,
                todas,
                filtradas.size(),
                usuarioRepository.findByEstado(EstadoUsuario.ACTIVA).size(),
                filtradas.stream().filter(fila -> "PENDIENTE".equals(fila.resultado())).count(),
                filtradas.stream().filter(fila -> "FALLIDO".equals(fila.resultado()) || "RECHAZADO".equals(fila.resultado())).count(),
                todas.stream().map(AuditoriaFilaDto::accion).distinct().sorted().toList(),
                todas.stream().map(AuditoriaFilaDto::modulo).distinct().sorted().toList(),
                alertas,
                actividad,
                frecuentes,
                actividad.stream().mapToLong(AuditoriaActividadDiaDto::cantidad).max().orElse(1),
                frecuentes.stream().mapToLong(AuditoriaConteoDto::cantidad).max().orElse(1)
        );
    }

    private AuditoriaFilaDto fila(Auditoria auditoria) {
        Usuario usuario = auditoria.getUsuario();
        String nombre = usuario == null ? "Sistema" : usuario.getUsername();
        String detalle = auditoria.getMotivo() == null || auditoria.getMotivo().isBlank()
                ? "Acción registrada en el sistema" : auditoria.getMotivo();
        return new AuditoriaFilaDto(auditoria.getFechaHora(),
                usuario == null ? null : usuario.getIdUsuario(), nombre,
                auditoria.getAccion(), etiquetaModulo(auditoria.getEntidad()), detalle,
                resultado(auditoria.getAccion(), auditoria.getDatosNuevos()));
    }

    private String resultado(String accion, String datosNuevos) {
        String texto = ((accion == null ? "" : accion) + " " + (datosNuevos == null ? "" : datosNuevos)).toUpperCase();
        if (texto.contains("PENDIENTE")) return "PENDIENTE";
        if (texto.contains("RECHAZ")) return "RECHAZADO";
        if (texto.contains("VENC")) return "VENCIDO";
        if (texto.contains("FALL")) return "FALLIDO";
        return "EXITOSO";
    }

    private String etiquetaModulo(String entidad) {
        if (entidad == null || entidad.isBlank()) return "Sistema";
        return switch (entidad.toUpperCase()) {
            case "SEGURIDAD" -> "Seguridad";
            case "CREDITO" -> "Créditos";
            case "PAGO" -> "Pagos";
            case "PLAN_REGULARIZACION" -> "Regularizaciones";
            case "USUARIO" -> "Usuarios";
            default -> entidad.substring(0, 1).toUpperCase() + entidad.substring(1).toLowerCase();
        };
    }

    private boolean vacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
