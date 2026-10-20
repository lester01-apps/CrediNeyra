package com.credineyra.controller;

import com.credineyra.dto.UsuarioRegistroDto;
import com.credineyra.entity.Usuario;
import com.credineyra.service.AuditoriaService;
import com.credineyra.service.PersonaService;
import com.credineyra.service.RolService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final RolService rolService;
    private final AuditoriaService auditoriaService;

    public UsuarioController(
            UsuarioService usuarioService,
            PersonaService personaService,
            RolService rolService,
            AuditoriaService auditoriaService
    ) {
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.rolService = rolService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(
            Model model,
            Authentication authentication
    ) {

        model.addAttribute(
                "usuarios",
                usuarioService.listarTodos()
        );

        cargarBase(model, authentication);

        return "admin/usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevoUsuario(
            Model model,
            Authentication authentication
    ) {

        prepararFormulario(model);
        cargarBase(model, authentication);

        return "admin/usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("usuarioDto")
            UsuarioRegistroDto dto,
            BindingResult result,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
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

            cargarBase(model, authentication);

            return "admin/usuarios/formulario";
        }

        try {
            Usuario usuarioCreado = usuarioService.registrarCliente(dto);
            registrarAuditoriaCreacion(usuarioCreado, authentication);
        } catch (RuntimeException ex) {
            model.addAttribute("mensajeError", ex.getMessage());
            model.addAttribute("personas", personaService.listarActivos());
            cargarBase(model, authentication);
            return "admin/usuarios/formulario";
        }

        redirectAttributes.addFlashAttribute(
                "mensajeExito",
                "Usuario creado correctamente. En su primer ingreso podrá conservar o cambiar la contraseña inicial."
        );

        return "redirect:/admin/usuarios";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model,
            Authentication authentication
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

        cargarBase(model, authentication);

        return "admin/usuarios/detalle";
    }

    private void prepararFormulario(
            Model model
    ) {

        UsuarioRegistroDto usuarioDto = new UsuarioRegistroDto();
        usuarioDto.setIdRol(
                rolService.buscarPorNombre("CLIENTE").getIdRol()
        );

        model.addAttribute(
                "usuarioDto",
                usuarioDto
        );

        model.addAttribute(
                "personas",
                personaService.listarActivos()
        );

    }

    private void cargarBase(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        model.addAttribute("seccionActiva", "usuarios");
    }

    private void registrarAuditoriaCreacion(
            Usuario usuarioCreado,
            Authentication authentication
    ) {
        Usuario administrador = usuarioService.buscarPorUsername(authentication.getName());

        auditoriaService.registrarEvento(
                administrador,
                "CREAR_USUARIO",
                "USUARIO",
                String.valueOf(usuarioCreado.getIdUsuario()),
                "Se creó la cuenta inicial para " + usuarioCreado.getUsername(),
                null,
                "{\"username\":\"" + usuarioCreado.getUsername() + "\"}",
                null
        );
    }
}
