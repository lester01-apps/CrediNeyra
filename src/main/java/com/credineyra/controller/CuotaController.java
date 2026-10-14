package com.credineyra.controller;

import com.credineyra.service.CreditoService;
import com.credineyra.service.CuotaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/cuotas")
public class CuotaController {

    private final CuotaService cuotaService;
    private final CreditoService creditoService;

    public CuotaController(
            CuotaService cuotaService,
            CreditoService creditoService
    ) {
        this.cuotaService = cuotaService;
        this.creditoService = creditoService;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "cuotas",
                cuotaService.listarTodos()
        );

        return "admin/cuotas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "cuota",
                cuotaService.buscarPorId(id)
        );

        return "admin/cuotas/detalle";
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
                "cuotas",
                cuotaService.listarPorCredito(
                        idCredito
                )
        );

        return "admin/cuotas/por-credito";
    }
}