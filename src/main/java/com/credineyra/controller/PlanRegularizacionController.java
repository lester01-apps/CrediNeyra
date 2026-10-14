package com.credineyra.controller;

import com.credineyra.dto.PlanRegularizacionRegistroDto;
import com.credineyra.entity.PlanRegularizacion;
import com.credineyra.entity.Usuario;
import com.credineyra.service.CreditoService;
import com.credineyra.service.PlanRegularizacionService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/regularizaciones")
public class PlanRegularizacionController {

    private final PlanRegularizacionService planService;
    private final CreditoService creditoService;
    private final UsuarioService usuarioService;

    public PlanRegularizacionController(
            PlanRegularizacionService planService,
            CreditoService creditoService,
            UsuarioService usuarioService
    ) {
        this.planService = planService;
        this.creditoService = creditoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/nuevo/{idCredito}")
    public String nuevo(
            @PathVariable Long idCredito,
            Model model
    ) {

        PlanRegularizacionRegistroDto dto =
                new PlanRegularizacionRegistroDto();

        dto.setIdCredito(idCredito);

        model.addAttribute(
                "planDto",
                dto
        );

        model.addAttribute(
                "credito",
                creditoService.buscarPorId(
                        idCredito
                )
        );

        return "admin/regularizaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("planDto")
            PlanRegularizacionRegistroDto dto,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "credito",
                    creditoService.buscarPorId(
                            dto.getIdCredito()
                    )
            );

            return "admin/regularizaciones/formulario";
        }

        Usuario usuario =
                usuarioService.buscarPorUsername(
                        authentication.getName()
                );

        PlanRegularizacion plan =
                planService.crearPlan(
                        dto,
                        usuario
                );

        return "redirect:/admin/regularizaciones/"
                + plan.getIdPlan();
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "plan",
                planService.buscarPorId(id)
        );

        model.addAttribute(
                "detalles",
                planService.listarDetalles(id)
        );

        return "admin/regularizaciones/detalle";
    }
}