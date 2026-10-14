package com.credineyra.controller;

import com.credineyra.service.ClienteService;
import com.credineyra.service.CreditoService;
import com.credineyra.service.PagoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioController {

    private final ClienteService clienteService;
    private final CreditoService creditoService;
    private final PagoService pagoService;


    public InicioController(
            ClienteService clienteService,
            CreditoService creditoService,
            PagoService pagoService
    ) {

        this.clienteService =
                clienteService;

        this.creditoService =
                creditoService;

        this.pagoService =
                pagoService;
    }


    /* =========================================================
       INICIO GENERAL
       ========================================================= */

    @GetMapping("/")
    public String inicio() {

        return "redirect:/inicio";

    }


    /* =========================================================
       LOGIN
       ========================================================= */

    @GetMapping("/login")
    public String login() {

        return "auth/login";

    }


    /* =========================================================
       REDIRECCIONAR SEGÚN ROL
       ========================================================= */

    @GetMapping("/inicio")
    public String redireccionarSegunRol(
            Authentication authentication
    ) {

        boolean esAdministrador =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_ADMINISTRADOR"
                                        )
                        );


        if (esAdministrador) {

            return "redirect:/admin";

        }


        boolean esCliente =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority
                                        .getAuthority()
                                        .equals(
                                                "ROLE_CLIENTE"
                                        )
                        );


        if (esCliente) {

            return "redirect:/cliente";

        }


        return "redirect:/login?error";
    }


    /* =========================================================
       DASHBOARD ADMINISTRADOR
       ========================================================= */

    @GetMapping("/admin")
    public String dashboardAdministrador(
            Authentication authentication,
            Model model
    ) {

        /* =====================================================
           USUARIO AUTENTICADO
           ===================================================== */

        model.addAttribute(
                "username",
                authentication.getName()
        );


        /* =====================================================
           TARJETAS PRINCIPALES
           ===================================================== */

        model.addAttribute(
                "totalClientes",
                clienteService.contarClientes()
        );


        model.addAttribute(
                "creditosActivos",
                creditoService.contarCreditosActivos()
        );


        model.addAttribute(
                "totalPagos",
                pagoService.contarPagos()
        );


        model.addAttribute(
                "creditosMora",
                creditoService.contarCreditosEnMora()
        );


        /* =====================================================
           ÚLTIMOS CLIENTES
           ===================================================== */

        model.addAttribute(
                "ultimosClientes",
                clienteService.listarUltimosCinco()
        );


        /* =====================================================
           CRÉDITOS RECIENTES
           ===================================================== */

        model.addAttribute(
                "creditosRecientes",
                creditoService.listarUltimosCinco()
        );


        /* =====================================================
           GRÁFICO DE PAGOS
           SEMANA
           ===================================================== */

        model.addAttribute(
                "pagosSemana",
                pagoService.obtenerPagosSemanaActual()
        );


        /* =====================================================
           GRÁFICO DE PAGOS
           MES
           ===================================================== */

        model.addAttribute(
                "pagosMes",
                pagoService.obtenerPagosMesActual()
        );


        /* =====================================================
           GRÁFICO DE PAGOS
           AÑO
           ===================================================== */

        model.addAttribute(
                "pagosAnio",
                pagoService.obtenerPagosAnioActual()
        );


        return "admin/dashboard";
    }

}