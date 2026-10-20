package com.credineyra.controller;

import com.credineyra.service.AuditoriaPanelService;
import com.credineyra.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/auditoria")
public class AuditoriaController {

    private final AuditoriaPanelService panelService;
    private final UsuarioService usuarioService;

    public AuditoriaController(AuditoriaPanelService panelService, UsuarioService usuarioService) {
        this.panelService = panelService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String panel(@RequestParam(required = false) LocalDate desde,
                        @RequestParam(required = false) LocalDate hasta,
                        @RequestParam(required = false) Long usuario,
                        @RequestParam(required = false) String accion,
                        @RequestParam(required = false) String modulo,
                        @RequestParam(required = false) String resultado,
                        Authentication authentication,
                        Model model) {
        LocalDate desdeSeguro = desde == null ? LocalDate.now().minusDays(30) : desde;
        LocalDate hastaSeguro = hasta == null ? LocalDate.now() : hasta;
        model.addAttribute("panel", panelService.obtener(desdeSeguro, hastaSeguro, usuario, accion, modulo, resultado));
        model.addAttribute("usuarios", usuarioService.listarTodos());
        model.addAttribute("desde", desdeSeguro);
        model.addAttribute("hasta", hastaSeguro);
        model.addAttribute("usuarioSeleccionado", usuario);
        model.addAttribute("accionSeleccionada", accion == null ? "" : accion);
        model.addAttribute("moduloSeleccionado", modulo == null ? "" : modulo);
        model.addAttribute("resultadoSeleccionado", resultado == null ? "" : resultado);
        model.addAttribute("username", authentication.getName());
        model.addAttribute("seccionActiva", "auditoria");
        return "admin/auditoria/panel";
    }
}
