package com.credineyra.controller;

import com.credineyra.entity.Usuario;
import com.credineyra.service.ClientePagoPanelService;
import com.credineyra.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Páginas informativas y de consulta del portal del cliente. */
@Controller
@RequestMapping("/cliente")
public class ClientePortalController {

    private static final DateTimeFormatter FECHA_ALERTA =
            DateTimeFormatter.ofPattern("dd MMM. yyyy", new Locale("es", "PE"));
    /** Los intereses/mora empiezan después de tres días completos de gracia. */
    private static final int DIAS_GRACIA_MORA = 3;

    private final UsuarioService usuarioService;
    private final ClientePagoPanelService panelService;

    public ClientePortalController(UsuarioService usuarioService, ClientePagoPanelService panelService) {
        this.usuarioService = usuarioService;
        this.panelService = panelService;
    }

    @GetMapping("/inicio")
    public String inicio(Authentication authentication, Model model) {
        cargarPanel(authentication, model, "inicio");
        var panel = (com.credineyra.dto.ClientePagoPanelDto) model.asMap().get("panel");
        var creditoProximo = panel.creditos().stream()
                .filter(c -> c.montoExigible() != null && c.montoExigible().signum() > 0)
                .min(Comparator.comparing(
                        c -> c.proximoVencimiento(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));
        BigDecimal capitalOriginal = panel.creditos().stream()
                .map(c -> c.capitalOriginal() == null ? BigDecimal.ZERO : c.capitalOriginal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal capitalPendiente = panel.creditos().stream()
                .map(c -> c.capitalPendiente() == null ? BigDecimal.ZERO : c.capitalPendiente())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("creditoProximo", creditoProximo.orElse(null));
        model.addAttribute("totalPrestado", capitalOriginal);
        model.addAttribute("avanceGeneral", porcentajePagado(capitalOriginal, capitalPendiente));
        model.addAttribute("pagosRecientes", panel.pagos().stream()
                .sorted(Comparator.comparing(
                        com.credineyra.dto.ClientePagoPanelDto.PagoFila::fechaPago,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(3)
                .toList());
        return "cliente/portal/inicio";
    }

    @GetMapping("/creditos")
    public String creditos(
            Authentication authentication,
            @RequestParam(required = false) Long credito,
            Model model
    ) {
        cargarPanel(authentication, model, "creditos");
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        var panel = (com.credineyra.dto.ClientePagoPanelDto) model.asMap().get("panel");
        var seleccionado = panel.creditos().stream()
                .filter(c -> credito == null || credito.equals(c.idCredito()))
                .findFirst()
                .or(() -> panel.creditos().stream()
                        .filter(c -> c.montoExigible() != null && c.montoExigible().signum() > 0)
                        .min(Comparator.comparing(c -> c.proximoVencimiento(), Comparator.nullsLast(Comparator.naturalOrder()))))
                .or(() -> panel.creditos().stream().findFirst());

        seleccionado.ifPresent(c -> model.addAttribute("creditoSeleccionado", c));
        seleccionado.ifPresent(c -> model.addAttribute("detalleSeleccionado", panelService.obtenerDetallePropio(c.idCredito(), usuario)));
        BigDecimal totalPrestado = panel.creditos().stream()
                .map(c -> c.capitalOriginal() == null ? BigDecimal.ZERO : c.capitalOriginal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activos = panel.creditos().stream().filter(c -> !"FINALIZADO".equals(c.estado())).count();
        BigDecimal avance = seleccionado.map(c -> porcentajePagado(c.capitalOriginal(), c.capitalPendiente())).orElse(BigDecimal.ZERO);
        model.addAttribute("totalPrestado", totalPrestado);
        model.addAttribute("creditosActivos", activos);
        model.addAttribute("avanceSeleccionado", avance);
        model.addAttribute("alertasCredito", crearAlertasCredito(panel));
        return "cliente/portal/creditos";
    }

    @GetMapping("/creditos/{idCredito}")
    public String detalleCredito(
            @PathVariable Long idCredito,
            Authentication authentication,
            Model model
    ) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        model.addAttribute("detalle", panelService.obtenerDetallePropio(idCredito, usuario));
        model.addAttribute("clienteNombre", usuario.getPersona().getNombres());
        model.addAttribute("seccionActiva", "creditos");
        return "cliente/portal/credito-detalle";
    }

    @GetMapping("/historial")
    public String historial(Authentication authentication, Model model) {
        cargarPanel(authentication, model, "historial");
        return "cliente/portal/historial";
    }

    @GetMapping("/sobre-nosotros")
    public String sobreNosotros(Authentication authentication, Model model) {
        cargarBase(authentication, model, "nosotros");
        return "cliente/portal/sobre-nosotros";
    }

    @GetMapping("/soporte")
    public String soporte(Authentication authentication, Model model) {
        cargarBase(authentication, model, "soporte");
        return "cliente/portal/soporte";
    }

    @GetMapping("/contacto")
    public String contacto(Authentication authentication, Model model) {
        cargarBase(authentication, model, "contacto");
        return "cliente/portal/contacto";
    }

    private void cargarPanel(Authentication authentication, Model model, String seccion) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        var panel = panelService.obtener(usuario);
        long pagosAprobados = panel.pagos().stream().filter(p -> "APROBADO".equals(p.estado())).count();
        long pagosPendientes = panel.pagos().stream().filter(p -> "PENDIENTE_VALIDACION".equals(p.estado())).count();
        long pagosRechazados = panel.pagos().stream().filter(p -> "RECHAZADO".equals(p.estado())).count();
        long creditosAlDia = panel.creditos().stream()
                .filter(c -> "ACTIVO".equals(c.estado()) || "AL_DIA".equals(c.estado()))
                .count();
        long creditosRegularizacion = panel.creditos().stream()
                .filter(c -> "EN_REGULARIZACION".equals(c.estado()) || "EN_MORA".equals(c.estado()) || "ATRASO_PROLONGADO".equals(c.estado()))
                .count();
        java.math.BigDecimal saldoTotal = panel.creditos().stream()
                .map(c -> c.totalPendiente() == null ? java.math.BigDecimal.ZERO : c.totalPendiente())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        model.addAttribute("panel", panel);
        model.addAttribute("pagosAprobados", pagosAprobados);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("pagosRechazados", pagosRechazados);
        model.addAttribute("creditosAlDia", creditosAlDia);
        model.addAttribute("creditosRegularizacion", creditosRegularizacion);
        model.addAttribute("saldoTotal", saldoTotal);
        Map<Long, EstadoVisualCredito> estadosVisuales = crearEstadosVisuales(panel);
        model.addAttribute("etiquetasEstadoCredito", estadosVisuales.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entrada -> entrada.getValue().etiqueta()
                )));
        model.addAttribute("clasesEstadoCredito", estadosVisuales.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entrada -> entrada.getValue().claseCss()
                )));
        model.addAttribute("clienteNombre", usuario.getPersona().getNombres());
        model.addAttribute("seccionActiva", seccion);
    }

    private void cargarBase(Authentication authentication, Model model, String seccion) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        model.addAttribute("clienteNombre", usuario.getPersona().getNombres());
        model.addAttribute("seccionActiva", seccion);
    }

    private BigDecimal porcentajePagado(BigDecimal original, BigDecimal pendiente) {
        if (original == null || original.signum() <= 0) return BigDecimal.ZERO;
        BigDecimal saldo = pendiente == null ? BigDecimal.ZERO : pendiente;
        return original.subtract(saldo).max(BigDecimal.ZERO)
                .multiply(BigDecimal.valueOf(100))
                .divide(original, 0, RoundingMode.HALF_UP);
    }

    private List<AlertaCredito> crearAlertasCredito(
            com.credineyra.dto.ClientePagoPanelDto panel
    ) {
        LocalDate hoy = LocalDate.now();
        List<AlertaCredito> alertas = new ArrayList<>();

        for (var credito : panel.creditos()) {
            if (credito.montoExigible() == null || credito.montoExigible().signum() <= 0) {
                continue;
            }

            LocalDate vencimiento = credito.proximoVencimiento();
            long diasVencida = vencimiento == null || !vencimiento.isBefore(hoy)
                    ? 0
                    : ChronoUnit.DAYS.between(vencimiento, hoy);

            if (diasVencida > DIAS_GRACIA_MORA) {
                alertas.add(new AlertaCredito(
                        "danger",
                        "Crédito en mora",
                        "La cuota " + credito.proximaCuota() + " del crédito "
                                + credito.codigoCredito() + " lleva " + diasVencida + " días vencida.",
                        "El período de gracia terminó. Regulariza tu pago para evitar más intereses.",
                        credito.idCredito()
                ));
                continue;
            }

            if (diasVencida > 0) {
                LocalDate finGracia = vencimiento.plusDays(DIAS_GRACIA_MORA);
                alertas.add(new AlertaCredito(
                        "due",
                        "Cuota vencida: período de gracia",
                        "La cuota " + credito.proximaCuota() + " del crédito "
                                + credito.codigoCredito() + " venció hace " + diasVencida + " día"
                                + (diasVencida == 1 ? "." : "s."),
                        "Puedes pagar hasta el " + finGracia.format(FECHA_ALERTA)
                                + " antes de que se considere en mora.",
                        credito.idCredito()
                ));
                continue;
            }

            if (vencimiento != null && vencimiento.isEqual(hoy)) {
                alertas.add(new AlertaCredito(
                        "due",
                        "Cuota disponible para pago",
                        "Tu cuota " + credito.proximaCuota() + " del crédito "
                                + credito.codigoCredito() + " vence hoy.",
                        "Ya puedes pagarla desde el panel de pagos.",
                        credito.idCredito()
                ));
                continue;
            }

            if (vencimiento != null
                    && ChronoUnit.DAYS.between(hoy, vencimiento) <= 7) {
                alertas.add(new AlertaCredito(
                        "due",
                        "Próxima cuota",
                        "Tu cuota " + credito.proximaCuota() + " del crédito "
                                + credito.codigoCredito() + " vence el "
                                + vencimiento.format(FECHA_ALERTA) + ".",
                        "Puedes revisarla y prepararte para el pago.",
                        credito.idCredito()
                ));
            }
        }

        return alertas;
    }

    private Map<Long, EstadoVisualCredito> crearEstadosVisuales(
            com.credineyra.dto.ClientePagoPanelDto panel
    ) {
        Map<Long, EstadoVisualCredito> estados = new HashMap<>();
        for (var credito : panel.creditos()) {
            estados.put(credito.idCredito(), determinarEstadoVisual(credito, LocalDate.now()));
        }
        return estados;
    }

    private EstadoVisualCredito determinarEstadoVisual(
            com.credineyra.dto.ClientePagoPanelDto.CreditoFila credito,
            LocalDate hoy
    ) {
        if (credito.montoExigible() == null || credito.montoExigible().signum() <= 0) {
            return new EstadoVisualCredito(
                    "FINALIZADO".equals(credito.estado()) ? "Finalizado" : "Al día",
                    "ok"
            );
        }

        LocalDate vencimiento = credito.proximoVencimiento();
        if (vencimiento != null && vencimiento.isBefore(hoy)) {
            long diasVencida = ChronoUnit.DAYS.between(vencimiento, hoy);
            if (diasVencida > DIAS_GRACIA_MORA) {
                return new EstadoVisualCredito("En mora", "danger");
            }
            return new EstadoVisualCredito("En gracia", "warning");
        }
        if (vencimiento != null && vencimiento.isEqual(hoy)) {
            return new EstadoVisualCredito("Vence hoy", "warning");
        }
        if (vencimiento != null && ChronoUnit.DAYS.between(hoy, vencimiento) <= 7) {
            return new EstadoVisualCredito("Próximo a vencer", "warning");
        }
        return new EstadoVisualCredito("Al día", "ok");
    }

    private record AlertaCredito(
            String tipo,
            String titulo,
            String mensaje,
            String detalle,
            Long idCredito
    ) {}

    private record EstadoVisualCredito(String etiqueta, String claseCss) {}
}
