package com.credineyra.controller;

import com.credineyra.dto.PlanRegularizacionRegistroDto;
import com.credineyra.entity.PlanRegularizacion;
import com.credineyra.entity.Usuario;
import com.credineyra.service.PlanRegularizacionService;
import com.credineyra.service.RegularizacionFormularioService;
import com.credineyra.service.RegularizacionPanelService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/regularizaciones")
public class PlanRegularizacionController {

    private final PlanRegularizacionService planService;

    private final RegularizacionPanelService panelService;

    private final RegularizacionFormularioService formularioService;

    private final UsuarioService usuarioService;


    public PlanRegularizacionController(
            PlanRegularizacionService planService,
            RegularizacionPanelService panelService,
            RegularizacionFormularioService formularioService,
            UsuarioService usuarioService
    ) {

        this.planService =
                planService;

        this.panelService =
                panelService;

        this.formularioService =
                formularioService;

        this.usuarioService =
                usuarioService;
    }


    /*
     * =========================================================
     * PANEL
     * =========================================================
     */

    @GetMapping
    public String panel(
            Model model
    ) {

        model.addAttribute(
                "panel",
                panelService.obtener()
        );

        model.addAttribute(
                "seccionActiva",
                "regularizaciones"
        );

        return "admin/regularizaciones/lista";
    }


    /*
     * =========================================================
     * NUEVO PLAN
     * =========================================================
     */

    @GetMapping("/nuevo/{idCredito}")
    public String nuevo(
            @PathVariable Long idCredito,
            Model model
    ) {

        PlanRegularizacionRegistroDto dto =
                new PlanRegularizacionRegistroDto();


        dto.setIdCredito(
                idCredito
        );


        model.addAttribute(
                "planDto",
                dto
        );


        model.addAttribute(
                "resumen",
                formularioService
                        .obtener(
                                idCredito
                        )
        );


        model.addAttribute(
                "seccionActiva",
                "regularizaciones"
        );


        return "admin/regularizaciones/formulario";
    }


    /*
     * =========================================================
     * GUARDAR PLAN
     * =========================================================
     */

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("planDto")
            PlanRegularizacionRegistroDto dto,

            BindingResult result,

            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (
                result.hasErrors()
        ) {

            model.addAttribute(
                    "resumen",
                    formularioService
                            .obtener(
                                    dto.getIdCredito()
                            )
            );


            model.addAttribute(
                    "seccionActiva",
                    "regularizaciones"
            );


            return "admin/regularizaciones/formulario";
        }


        try {

            Usuario usuario =
                    usuarioService
                            .buscarPorUsername(
                                    authentication
                                            .getName()
                            );


            PlanRegularizacion plan =
                    planService
                            .crearPlan(
                                    dto,
                                    usuario
                            );


            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Plan de regularización creado correctamente."
                    );


            return "redirect:/admin/regularizaciones/"
                    + plan.getIdPlan();


        } catch (RuntimeException ex) {


            model.addAttribute(
                    "mensajeError",
                    ex.getMessage()
            );


            model.addAttribute(
                    "resumen",
                    formularioService
                            .obtener(
                                    dto.getIdCredito()
                            )
            );


            model.addAttribute(
                    "seccionActiva",
                    "regularizaciones"
            );


            return "admin/regularizaciones/formulario";
        }
    }


    /*
     * =========================================================
     * DETALLE
     * =========================================================
     */

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "plan",
                planService
                        .buscarPorId(id)
        );


        model.addAttribute(
                "detalles",
                planService
                        .listarDetalles(id)
        );


        model.addAttribute(
                "seccionActiva",
                "regularizaciones"
        );


        return "admin/regularizaciones/detalle";
    }
}