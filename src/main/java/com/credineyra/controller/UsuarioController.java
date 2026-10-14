package com.credineyra.controller;

import com.credineyra.dto.UsuarioRegistroDto;
import com.credineyra.service.PersonaService;
import com.credineyra.service.RolService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final RolService rolService;

    public UsuarioController(
            UsuarioService usuarioService,
            PersonaService personaService,
            RolService rolService
    ) {
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.rolService = rolService;
    }

    @GetMapping
    public String listar(
            Model model
    ) {

        model.addAttribute(
                "usuarios",
                usuarioService.listarTodos()
        );

        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(
            Model model
    ) {

        prepararFormulario(model);

        return "admin/usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("usuarioDto")
            UsuarioRegistroDto dto,
            BindingResult result,
            Model model
    ) {

        if (dto.getPassword() != null
                && !dto.getPassword()
                .equals(
                        dto.getConfirmarPassword()
                )) {

            result.rejectValue(
                    "confirmarPassword",
                    "password.no.coincide",
                    "Las contraseñas no coinciden"
            );
        }

        if (result.hasErrors()) {

            model.addAttribute(
                    "personas",
                    personaService.listarActivos()
            );

            model.addAttribute(
                    "roles",
                    rolService.listarActivos()
            );

            return "admin/usuarios/formulario";
        }

        usuarioService.registrar(dto);

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "usuario",
                usuarioService.buscarPorId(id)
        );

        model.addAttribute(
                "rolesUsuario",
                usuarioService
                        .listarRolesUsuario(id)
        );

        return "admin/usuarios/detalle";
    }

    private void prepararFormulario(
            Model model
    ) {

        model.addAttribute(
                "usuarioDto",
                new UsuarioRegistroDto()
        );

        model.addAttribute(
                "personas",
                personaService.listarActivos()
        );

        model.addAttribute(
                "roles",
                rolService.listarActivos()
        );
    }
}