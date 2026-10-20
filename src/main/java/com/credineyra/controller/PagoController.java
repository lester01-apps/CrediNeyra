package com.credineyra.controller;

import com.credineyra.dto.PagoAprobacionDto;
import com.credineyra.dto.PagoRegistroDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Usuario;
import com.credineyra.service.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/admin/pagos")
public class PagoController {

    private final PagoService pagoService;

    private final PagoPanelService pagoPanelService;

    private final PagoDetalleVistaService detalleVistaService;

    private final PagoValidacionService validacionService;

    private final CreditoService creditoService;

    private final UsuarioService usuarioService;

    private final ConstanciaPagoService constanciaPagoService;

    public PagoController(
            PagoService pagoService,
            PagoPanelService pagoPanelService,
            PagoDetalleVistaService detalleVistaService,
            PagoValidacionService validacionService,
            CreditoService creditoService,
            UsuarioService usuarioService,
            ConstanciaPagoService constanciaPagoService
    ) {

        this.pagoService =
                pagoService;

        this.pagoPanelService =
                pagoPanelService;

        this.detalleVistaService =
                detalleVistaService;

        this.validacionService =
                validacionService;

        this.creditoService =
                creditoService;

        this.usuarioService =
                usuarioService;

        this.constanciaPagoService =
                constanciaPagoService;
    }

    @GetMapping
    public String listar(
            Model model
    ) {

        model.addAttribute(
                "panel",
                pagoPanelService
                        .obtenerPanel()
        );

        model.addAttribute(
                "seccionActiva",
                "pagos"
        );

        return "admin/pagos/lista";
    }

    @GetMapping("/nuevo/{idCredito}")
    public String nuevoCredito(
            @PathVariable Long idCredito,
            Model model
    ) {

        PagoRegistroDto dto =
                new PagoRegistroDto();

        dto.setIdCredito(
                idCredito
        );

        model.addAttribute(
                "pagoDto",
                dto
        );

        model.addAttribute(
                "credito",
                creditoService
                        .buscarPorId(
                                idCredito
                        )
        );

        model.addAttribute(
                "seccionActiva",
                "pagos"
        );

        return "admin/pagos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("pagoDto")
            PagoRegistroDto dto,

            BindingResult result,

            Authentication authentication,

            Model model
    ) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "seccionActiva",
                    "pagos"
            );

            return "admin/pagos/formulario";
        }

        Credito credito =
                creditoService
                        .buscarPorId(
                                dto.getIdCredito()
                        );

        Usuario usuario =
                usuarioService
                        .buscarPorUsername(
                                authentication.getName()
                        );

        Pago pago =
                new Pago();

        pago.setCredito(
                credito
        );

        pago.setRegistradoPor(
                usuario
        );

        pago.setMonto(
                dto.getMonto()
        );

        pago.setMetodoPago(
                dto.getMetodoPago()
        );

        pago.setReferencia(
                dto.getReferencia()
        );

        pago.setObservaciones(
                dto.getObservaciones()
        );

        pago.setIdempotencyKey(
                UUID.randomUUID()
                        .toString()
        );

        Pago guardado =
                pagoService
                        .registrar(
                                pago
                        );

        return "redirect:/admin/pagos/"
                + guardado.getIdPago();
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "detalle",
                detalleVistaService
                        .obtener(id)
        );

        model.addAttribute(
                "aprobacionDto",
                new PagoAprobacionDto()
        );

        model.addAttribute(
                "seccionActiva",
                "pagos"
        );

        return "admin/pagos/detalle";
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(
            @PathVariable Long id,

            @ModelAttribute("aprobacionDto")
            PagoAprobacionDto dto,

            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {

        try {

            Usuario usuario =
                    usuarioService
                            .buscarPorUsername(
                                    authentication.getName()
                            );

            var resultado =
                    validacionService
                            .aprobar(
                                    id,
                                    usuario,
                                    dto
                            );

            if (
                    resultado.advertencia() == null
            ) {

                redirectAttributes
                        .addFlashAttribute(
                                "mensajeExito",
                                "Pago aprobado y constancia generada correctamente."
                        );

            } else {

                redirectAttributes
                        .addFlashAttribute(
                                "mensajeAdvertencia",
                                resultado.advertencia()
                        );
            }

        } catch (RuntimeException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            ex.getMessage()
                    );
        }

        return "redirect:/admin/pagos/"
                + id;
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(
            @PathVariable Long id,

            @RequestParam String motivo,

            Authentication authentication,

            RedirectAttributes redirectAttributes
    ) {

        try {

            if (
                    motivo == null
                            ||
                            motivo.isBlank()
            ) {

                throw new IllegalArgumentException(
                        "Debes indicar el motivo del rechazo"
                );
            }

            Usuario usuario =
                    usuarioService
                            .buscarPorUsername(
                                    authentication.getName()
                            );

            validacionService
                    .rechazar(
                            id,
                            usuario,
                            motivo
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "El pago fue rechazado."
                    );

        } catch (RuntimeException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            ex.getMessage()
                    );
        }

        return "redirect:/admin/pagos/"
                + id;
    }

    @PostMapping("/{id}/constancia")
    public String regenerarConstancia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {

            constanciaPagoService
                    .generarSiFalta(id);

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Constancia disponible."
                    );

        } catch (RuntimeException ex) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            ex.getMessage()
                    );
        }

        return "redirect:/admin/pagos/"
                + id;
    }
}