package com.credineyra.controller;

import com.credineyra.dto.PagoAprobacionDto;
import com.credineyra.dto.PagoRegistroDto;
import com.credineyra.entity.Credito;
import com.credineyra.entity.Pago;
import com.credineyra.entity.Usuario;
import com.credineyra.service.CreditoService;
import com.credineyra.service.PagoService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final CreditoService creditoService;
    private final UsuarioService usuarioService;

    public PagoController(
            PagoService pagoService,
            CreditoService creditoService,
            UsuarioService usuarioService
    ) {
        this.pagoService = pagoService;
        this.creditoService = creditoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "pagos",
                pagoService.listarTodos()
        );

        return "admin/pagos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(
            Model model
    ) {

        model.addAttribute(
                "pagoDto",
                new PagoRegistroDto()
        );

        return "admin/pagos/formulario";
    }

    @GetMapping("/nuevo/{idCredito}")
    public String nuevoCredito(
            @PathVariable Long idCredito,
            Model model
    ) {

        PagoRegistroDto dto =
                new PagoRegistroDto();

        dto.setIdCredito(idCredito);

        model.addAttribute(
                "pagoDto",
                dto
        );

        model.addAttribute(
                "credito",
                creditoService.buscarPorId(
                        idCredito
                )
        );

        return "admin/pagos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("pagoDto")
            PagoRegistroDto dto,
            BindingResult result,
            Authentication authentication
    ) {

        if (result.hasErrors()) {
            return "admin/pagos/formulario";
        }

        Credito credito =
                creditoService.buscarPorId(
                        dto.getIdCredito()
                );

        Usuario usuario =
                usuarioService
                        .buscarPorUsername(
                                authentication.getName()
                        );

        Pago pago = new Pago();

        pago.setCredito(credito);
        pago.setRegistradoPor(usuario);
        pago.setMonto(dto.getMonto());
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
                pagoService.registrar(pago);

        return "redirect:/admin/pagos/"
                + guardado.getIdPago();
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(
            @PathVariable Long id,
            @ModelAttribute("aprobacionDto")
            PagoAprobacionDto dto,
            Authentication authentication
    ) {

        Usuario usuario =
                usuarioService
                        .buscarPorUsername(
                                authentication.getName()
                        );

        pagoService.aprobar(
                id,
                usuario,
                dto
        );

        return "redirect:/admin/pagos/" + id;
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(
            @PathVariable Long id,
            @RequestParam(
                    required = false
            )
            String motivo,
            Authentication authentication
    ) {

        Usuario usuario =
                usuarioService
                        .buscarPorUsername(
                                authentication.getName()
                        );

        pagoService.rechazar(
                id,
                usuario,
                motivo
        );

        return "redirect:/admin/pagos/" + id;
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "pago",
                pagoService.buscarPorId(id)
        );

        model.addAttribute(
                "aprobacionDto",
                new PagoAprobacionDto()
        );

        return "admin/pagos/detalle";
    }

    @GetMapping("/credito/{idCredito}")
    public String porCredito(
            @PathVariable Long idCredito,
            Model model
    ) {

        model.addAttribute(
                "credito",
                creditoService.buscarPorId(
                        idCredito
                )
        );

        model.addAttribute(
                "pagos",
                pagoService.listarPorCredito(
                        idCredito
                )
        );

        return "admin/pagos/por-credito";
    }
}