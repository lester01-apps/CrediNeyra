package com.credineyra.controller;

import com.credineyra.dto.PagoClienteRegistroDto;
import com.credineyra.dto.PagoEfectivoCitaDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Usuario;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.service.ClientePagoPanelService;
import com.credineyra.service.PagoPresentacionService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@Controller
@RequestMapping("/cliente/pagos")
public class ClientePagoController {

    private final UsuarioService usuarioService;

    private final ClientePagoPanelService panelService;

    private final PagoPresentacionService presentacionService;

    @Value("${credineyra.pagos.yape.numero:973860581}")
    private String yapeNumero;

    @Value("${credineyra.pagos.yape.titular:Fransisco Neyra Mijahuanga}")
    private String yapeTitular;

    @Value("${credineyra.pagos.yape.qr-url:}")
    private String yapeQrUrl;

    @Value("${credineyra.whatsapp.admin-numero:51973860581}")
    private String whatsappAdministrador;

    @Value("${credineyra.pagos.efectivo.mapa-url:https://maps.app.goo.gl/Nukh3SaFxVjLiC5DA}")
    private String mapaUrl;

    public ClientePagoController(
            UsuarioService usuarioService,
            ClientePagoPanelService panelService,
            PagoPresentacionService presentacionService
    ) {

        this.usuarioService =
                usuarioService;

        this.panelService =
                panelService;

        this.presentacionService =
                presentacionService;
    }

    @GetMapping
    public String panel(
            Authentication authentication,
            Model model
    ) {

        Usuario usuario =
                usuario(authentication);

        var panel = panelService.obtener(usuario);
        var creditoProximo = panel.creditos().stream()
                .filter(c -> c.montoExigible() != null && c.montoExigible().signum() > 0)
                .min(Comparator.comparing(
                        c -> c.proximoVencimiento(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));

        if (creditoProximo.isPresent()) {
            Long idCredito = creditoProximo.get().idCredito();
            PagoClienteRegistroDto pagoDto = panelService.crearRegistroInicial(idCredito, usuario);
            model.addAttribute("pagoDto", pagoDto);

            PagoEfectivoCitaDto citaDto = new PagoEfectivoCitaDto();
            citaDto.setIdCredito(idCredito);
            citaDto.setMonto(pagoDto.getMonto());
            model.addAttribute("citaEfectivoDto", citaDto);
            cargarVistaPago(idCredito, usuario, model);
            return "cliente/pagos/nuevo";
        }

        model.addAttribute("panel", panel);
        model.addAttribute("pagosAprobados", panel.pagos().stream().filter(p -> "APROBADO".equals(p.estado())).count());
        model.addAttribute("pagosPendientes", panel.pagos().stream().filter(p -> "PENDIENTE_VALIDACION".equals(p.estado())).count());
        model.addAttribute("montoPagado", panel.pagos().stream().filter(p -> "APROBADO".equals(p.estado())).map(p -> p.monto() == null ? BigDecimal.ZERO : p.monto()).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("clienteNombre", panel.clienteNombre());
        model.addAttribute("seccionActiva", "pagos");

        cargarYape(
                model
        );

        return "cliente/pagos/lista";
    }

    @GetMapping("/nuevo/{idCredito}")
    public String nuevo(
            @PathVariable Long idCredito,
            @RequestParam(value = "tipoPago", defaultValue = "CUOTA_COMPLETA") String tipoPago,
            @RequestParam(value = "monto", required = false) BigDecimal montoLibre,
            Authentication authentication,
            Model model
    ) {

        Usuario usuario =
                usuario(authentication);

        Credito credito =
                panelService
                        .obtenerCreditoPropio(
                                idCredito,
                                usuario
                        );

        PagoClienteRegistroDto pagoDto = panelService
                .crearRegistroInicial(idCredito, usuario);
        aplicarMontoSeleccionado(pagoDto, idCredito, usuario, tipoPago, montoLibre);
        model.addAttribute("pagoDto", pagoDto);

        PagoEfectivoCitaDto citaDto = new PagoEfectivoCitaDto();
        citaDto.setIdCredito(credito.getIdCredito());
        citaDto.setMonto(pagoDto.getMonto());
        citaDto.setTipoPago(pagoDto.getTipoPago());
        model.addAttribute("citaEfectivoDto", citaDto);

        cargarVistaPago(idCredito, usuario, model);

        return "cliente/pagos/nuevo";
    }

    @GetMapping("/nuevo/{idCredito}/yape")
    public String pagarConYape(
            @PathVariable Long idCredito,
            @RequestParam(value = "tipoPago", defaultValue = "CUOTA_COMPLETA") String tipoPago,
            @RequestParam(value = "monto", required = false) BigDecimal montoLibre,
            Authentication authentication,
            Model model
    ) {
        prepararPanelMetodo(idCredito, usuario(authentication), model, "yape", tipoPago, montoLibre);
        return "cliente/pagos/nuevo";
    }

    @GetMapping("/nuevo/{idCredito}/efectivo")
    public String pagarEnEfectivo(
            @PathVariable Long idCredito,
            @RequestParam(value = "tipoPago", defaultValue = "CUOTA_COMPLETA") String tipoPago,
            @RequestParam(value = "monto", required = false) BigDecimal montoLibre,
            Authentication authentication,
            Model model
    ) {
        prepararPanelMetodo(idCredito, usuario(authentication), model, "efectivo", tipoPago, montoLibre);
        return "cliente/pagos/nuevo";
    }

    @PostMapping("/agendar-efectivo")
    public String agendarEfectivo(
            @Valid @ModelAttribute("citaEfectivoDto") PagoEfectivoCitaDto dto,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Usuario usuario = usuario(authentication);

        if (result.hasErrors()) {
            model.addAttribute("metodoActivo", "efectivo");
            model.addAttribute("panelPago", "efectivo");
            cargarFormularioPago(dto.getIdCredito(), usuario, model);
            return "cliente/pagos/nuevo";
        }

        try {
            dto.setMonto(resolverMontoPago(dto.getIdCredito(), usuario, dto.getTipoPago(), dto.getMonto()));
            Pago pago = presentacionService.agendarPagoEfectivo(dto, usuario);
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Cita de pago en efectivo registrada. Te esperamos en la fecha y hora indicadas.");
            crearEnlaceWhatsApp(dto, usuario, pago).ifPresent(url ->
                    redirectAttributes.addFlashAttribute("whatsappAdminUrl", url));
            return "redirect:/cliente/pagos";
        } catch (RuntimeException ex) {
            model.addAttribute("mensajeError", ex.getMessage());
            model.addAttribute("metodoActivo", "efectivo");
            model.addAttribute("panelPago", "efectivo");
            cargarFormularioPago(dto.getIdCredito(), usuario, model);
            return "cliente/pagos/nuevo";
        }
    }

    @PostMapping(
            value = "/enviar",
            consumes = "multipart/form-data"
    )
    public String enviar(
            @Valid
            @ModelAttribute("pagoDto")
            PagoClienteRegistroDto dto,

            BindingResult result,

            @RequestParam(
                    value = "comprobante",
                    required = false
            )
            MultipartFile comprobante,

            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        Usuario usuario =
                usuario(authentication);

        if (
                result.hasErrors()
        ) {
            model.addAttribute("panelPago", "yape");
            model.addAttribute("metodoActivo", "yape");
            cargarVistaPago(dto.getIdCredito(), usuario, model);
            agregarCitaSiFalta(dto.getIdCredito(), dto.getMonto(), usuario, model);

            return "cliente/pagos/nuevo";
        }

        try {
            dto.setMonto(resolverMontoPago(dto.getIdCredito(), usuario, dto.getTipoPago(), dto.getMonto()));

            Pago pago =
                    presentacionService
                            .presentarYape(
                                    dto,
                                    comprobante,
                                    usuario
                            );

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Comprobante enviado. "
                                    + "El pago PAG-"
                                    + String.format(
                                    "%06d",
                                    pago.getIdPago()
                            )
                                    + " está pendiente de validación."
                    );

            return "redirect:/cliente/pagos";

        } catch (RuntimeException ex) {

            model.addAttribute(
                    "mensajeError",
                    ex.getMessage()
            );
            model.addAttribute("panelPago", "yape");
            model.addAttribute("metodoActivo", "yape");

            cargarVistaPago(dto.getIdCredito(), usuario, model);
            agregarCitaSiFalta(dto.getIdCredito(), dto.getMonto(), usuario, model);

            return "cliente/pagos/nuevo";
        }
    }

    private Usuario usuario(
            Authentication authentication
    ) {

        return usuarioService
                .buscarPorUsername(
                        authentication.getName()
                );
    }

    private void cargarYape(
            Model model
    ) {

        model.addAttribute(
                "yapeNumero",
                yapeNumero
        );

        model.addAttribute(
                "yapeTitular",
                yapeTitular
        );

        model.addAttribute(
                "yapeQrUrl",
                yapeQrUrl
        );
        model.addAttribute("mapaUrl", mapaUrl);
        model.addAttribute("whatsappUrl", "https://wa.me/" + whatsappAdministrador.replaceAll("\\D", ""));
    }

    private void cargarFormularioPago(Long idCredito, Usuario usuario, Model model) {
        if (idCredito != null) {
            cargarVistaPago(idCredito, usuario, model);
            if (!model.containsAttribute("pagoDto")) {
                model.addAttribute("pagoDto", panelService.crearRegistroInicial(idCredito, usuario));
            }
            agregarCitaSiFalta(idCredito, null, usuario, model);
        }
    }

    private void prepararPanelMetodo(
            Long idCredito,
            Usuario usuario,
            Model model,
            String panelPago,
            String tipoPago,
            BigDecimal montoLibre
    ) {
        PagoClienteRegistroDto pagoDto = panelService.crearRegistroInicial(idCredito, usuario);
        aplicarMontoSeleccionado(pagoDto, idCredito, usuario, tipoPago, montoLibre);
        model.addAttribute("pagoDto", pagoDto);

        PagoEfectivoCitaDto citaDto = new PagoEfectivoCitaDto();
        citaDto.setIdCredito(idCredito);
        citaDto.setMonto(pagoDto.getMonto());
        citaDto.setTipoPago(pagoDto.getTipoPago());
        model.addAttribute("citaEfectivoDto", citaDto);
        model.addAttribute("panelPago", panelPago);
        model.addAttribute("metodoActivo", panelPago);
        cargarVistaPago(idCredito, usuario, model);
    }

    private void cargarVistaPago(Long idCredito, Usuario usuario, Model model) {
        var detalle = panelService.obtenerDetallePropio(idCredito, usuario);
        model.addAttribute("detalle", detalle);
        BigDecimal montoIntereses = detalle.cuotas().stream()
                .filter(c -> c.numero() != null && c.numero().equals(detalle.cuotaActual()))
                .map(c -> c.interes() == null ? BigDecimal.ZERO : c.interes())
                .findFirst()
                .orElse(BigDecimal.ZERO);
        model.addAttribute("montoSoloIntereses", montoIntereses);
        model.addAttribute("etiquetaVencimiento", etiquetaVencimiento(detalle.proximaCuota()));
        var panel = panelService.obtener(usuario);
        model.addAttribute("clienteNombre", panel.clienteNombre());
        model.addAttribute("clienteDni", panel.clienteDni());
        model.addAttribute("seccionActiva", "pagos");
        cargarYape(model);
    }

    private void aplicarMontoSeleccionado(
            PagoClienteRegistroDto dto,
            Long idCredito,
            Usuario usuario,
            String tipoPago,
            BigDecimal montoLibre
    ) {
        String tipoNormalizado = tipoPago == null ? "CUOTA_COMPLETA" : tipoPago.trim();
        dto.setTipoPago(tipoNormalizado);
        dto.setMonto(resolverMontoPago(idCredito, usuario, tipoNormalizado, montoLibre));
    }

    private BigDecimal resolverMontoPago(
            Long idCredito,
            Usuario usuario,
            String tipoPago,
            BigDecimal montoLibre
    ) {
        var detalle = panelService.obtenerDetallePropio(idCredito, usuario);
        BigDecimal totalCuota = detalle.montoExigible() == null
                ? BigDecimal.ZERO : detalle.montoExigible();
        BigDecimal intereses = detalle.cuotas().stream()
                .filter(c -> c.numero() != null && c.numero().equals(detalle.cuotaActual()))
                .map(c -> c.interes() == null ? BigDecimal.ZERO : c.interes())
                .findFirst()
                .orElse(BigDecimal.ZERO);
        String tipo = tipoPago == null ? "CUOTA_COMPLETA" : tipoPago.trim();

        return switch (tipo) {
            case "CUOTA_COMPLETA" -> totalCuota;
            case "SOLO_INTERESES" -> {
                if (intereses.signum() <= 0) {
                    throw new ReglaNegocioException("Esta cuota no tiene intereses pendientes para pagar");
                }
                yield intereses;
            }
            case "PAGO_LIBRE" -> {
                if (montoLibre == null || montoLibre.signum() <= 0) {
                    throw new ReglaNegocioException("Ingresa el monto que deseas pagar");
                }
                if (montoLibre.compareTo(totalCuota) > 0) {
                    throw new ReglaNegocioException("El pago libre no puede superar el monto pendiente de la cuota");
                }
                yield montoLibre;
            }
            default -> throw new ReglaNegocioException("El tipo de pago seleccionado no es válido");
        };
    }

    private void agregarCitaSiFalta(Long idCredito, BigDecimal monto, Usuario usuario, Model model) {
        if (model.containsAttribute("citaEfectivoDto")) {
            return;
        }
        PagoEfectivoCitaDto cita = new PagoEfectivoCitaDto();
        cita.setIdCredito(idCredito);
        cita.setMonto(monto == null ? panelService.crearRegistroInicial(idCredito, usuario).getMonto() : monto);
        model.addAttribute("citaEfectivoDto", cita);
    }

    private String etiquetaVencimiento(LocalDate fecha) {
        if (fecha == null) return "Pendiente de programación";
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fecha);
        if (dias < 0) return "Cuota vencida";
        if (dias == 0) return "Vence hoy";
        if (dias == 1) return "Vence mañana";
        return "Próximo a vencer";
    }

    private java.util.Optional<String> crearEnlaceWhatsApp(
            PagoEfectivoCitaDto cita, Usuario usuario, Pago pago
    ) {
        String numero = whatsappAdministrador == null ? "" : whatsappAdministrador.replaceAll("\\D", "");
        if (numero.isBlank()) {
            return java.util.Optional.empty();
        }
        String mensaje = "Nueva cita de pago en efectivo\nCliente: "
                + usuario.getPersona().getNombres() + " " + usuario.getPersona().getApellidos()
                + "\nCrédito: " + pago.getCredito().getCodigoCredito()
                + "\nMonto: S/ " + pago.getMonto()
                + "\nFecha: " + cita.getFecha() + " " + cita.getHora();
        return java.util.Optional.of("https://wa.me/" + numero + "?text="
                + URLEncoder.encode(mensaje, StandardCharsets.UTF_8));
    }
}
