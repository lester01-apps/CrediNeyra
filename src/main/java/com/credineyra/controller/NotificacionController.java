package com.credineyra.controller;

import com.credineyra.dto.NotificacionDto;
import com.credineyra.entity.Notificacion;
import com.credineyra.service.ClienteService;
import com.credineyra.service.CreditoService;
import com.credineyra.service.CuotaService;
import com.credineyra.service.NotificacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final ClienteService clienteService;
    private final CreditoService creditoService;
    private final CuotaService cuotaService;

    public NotificacionController(
            NotificacionService notificacionService,
            ClienteService clienteService,
            CreditoService creditoService,
            CuotaService cuotaService
    ) {
        this.notificacionService =
                notificacionService;

        this.clienteService =
                clienteService;

        this.creditoService =
                creditoService;

        this.cuotaService =
                cuotaService;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "notificaciones",
                notificacionService.listarTodos()
        );

        return "admin/notificaciones/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {

        model.addAttribute(
                "notificacionDto",
                new NotificacionDto()
        );

        model.addAttribute(
                "clientes",
                clienteService.listarActivos()
        );

        return "admin/notificaciones/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute("notificacionDto")
            NotificacionDto dto
    ) {

        Notificacion notificacion =
                new Notificacion();

        notificacion.setCliente(
                clienteService.buscarPorId(
                        dto.getIdCliente()
                )
        );

        if (dto.getIdCredito() != null) {

            notificacion.setCredito(
                    creditoService.buscarPorId(
                            dto.getIdCredito()
                    )
            );
        }

        if (dto.getIdCuota() != null) {

            notificacion.setCuota(
                    cuotaService.buscarPorId(
                            dto.getIdCuota()
                    )
            );
        }

        notificacion.setTipo(
                dto.getTipo()
        );

        notificacion.setCanal(
                dto.getCanal()
        );

        notificacion.setTitulo(
                dto.getTitulo()
        );

        notificacion.setMensaje(
                dto.getMensaje()
        );

        notificacion.setFechaProgramada(
                dto.getFechaProgramada()
        );

        notificacionService.registrar(
                notificacion
        );

        return "redirect:/admin/notificaciones";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "notificacion",
                notificacionService.buscarPorId(id)
        );

        return "admin/notificaciones/detalle";
    }
}