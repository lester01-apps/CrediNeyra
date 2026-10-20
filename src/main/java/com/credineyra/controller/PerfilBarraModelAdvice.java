package com.credineyra.controller;

import com.credineyra.service.AdministradorPerfilFotoService;
import com.credineyra.service.ClientePerfilService;
import com.credineyra.service.UsuarioService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Provee el estado del avatar para las cabeceras sin exponer rutas de Storage. */
@ControllerAdvice(annotations = Controller.class)
public class PerfilBarraModelAdvice {
    private final UsuarioService usuarioService;
    private final ClientePerfilService clientePerfilService;
    private final AdministradorPerfilFotoService administradorFotoService;

    public PerfilBarraModelAdvice(UsuarioService usuarioService,
                                  ClientePerfilService clientePerfilService,
                                  AdministradorPerfilFotoService administradorFotoService) {
        this.usuarioService = usuarioService;
        this.clientePerfilService = clientePerfilService;
        this.administradorFotoService = administradorFotoService;
    }

    @ModelAttribute
    public void agregarFotoPerfil(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) return;
        var usuario = usuarioService.buscarPorUsername(authentication.getName());
        boolean cliente = authentication.getAuthorities().stream()
                .anyMatch(rol -> "ROLE_CLIENTE".equals(rol.getAuthority()));
        boolean administrador = authentication.getAuthorities().stream()
                .anyMatch(rol -> "ROLE_ADMINISTRADOR".equals(rol.getAuthority()));
        if (cliente) model.addAttribute("clienteTieneFotoPerfil", clientePerfilService.obtenerFotoPropia(usuario).isPresent());
        if (administrador) {
            model.addAttribute("administradorTieneFotoPerfil", administradorFotoService.obtenerRuta(usuario).isPresent());
            model.addAttribute("administradorInicial", usuario.getPersona().getNombres().substring(0, 1).toUpperCase());
        }
    }
}
