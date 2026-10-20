package com.credineyra.controller;

import com.credineyra.dto.RestablecerPasswordDto;
import com.credineyra.entity.Usuario;
import com.credineyra.service.RecuperacionPasswordService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Cambio de clave para administradores autenticados mediante correo verificado. */
@Controller
@RequestMapping("/admin/seguridad")
public class AdminSeguridadController {

    private final UsuarioService usuarioService;
    private final RecuperacionPasswordService recuperacionPasswordService;

    public AdminSeguridadController(UsuarioService usuarioService,
                                    RecuperacionPasswordService recuperacionPasswordService) {
        this.usuarioService = usuarioService;
        this.recuperacionPasswordService = recuperacionPasswordService;
    }

    @PostMapping("/solicitar-codigo")
    public String solicitarCodigo(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            recuperacionPasswordService.solicitarCambioPassword(usuario(authentication));
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Enviamos un código temporal al correo registrado de tu cuenta.");
            return "redirect:/admin/seguridad/cambiar-password";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No se pudo enviar el código. Verifica que la cuenta tenga un correo disponible.");
            return "redirect:/admin/perfil";
        }
    }

    @ModelAttribute("restablecerDto")
    public RestablecerPasswordDto prepararFormularioCambio() {
        return new RestablecerPasswordDto();
    }

    @GetMapping("/cambiar-password")
    public String formularioCambio(Authentication authentication) {
        usuario(authentication);
        return "admin/seguridad/cambiar-password";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(@Valid @ModelAttribute("restablecerDto") RestablecerPasswordDto restablecerDto,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model,
                                  @RequestParam(required = false) String reenviar) {
        Usuario usuario = usuario(authentication);
        if (reenviar != null) {
            try {
                recuperacionPasswordService.solicitarCambioPassword(usuario);
                model.addAttribute("mensajeExito", "Enviamos un nuevo código a tu correo registrado.");
            } catch (RuntimeException ex) {
                model.addAttribute("mensajeError", "No se pudo reenviar el código. Inténtalo nuevamente.");
            }
            return "admin/seguridad/cambiar-password";
        }

        if (restablecerDto.getPassword() != null
                && !restablecerDto.getPassword().equals(restablecerDto.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "password.no.coincide", "Las contraseñas no coinciden");
        }
        if (result.hasErrors()) {
            return "admin/seguridad/cambiar-password";
        }

        try {
            recuperacionPasswordService.restablecerPasswordPrimerIngreso(
                    usuario.getIdUsuario(), restablecerDto.getToken(),
                    restablecerDto.getPassword(), restablecerDto.getConfirmarPassword());
            return "redirect:/admin/perfil?passwordActualizada";
        } catch (RuntimeException ex) {
            model.addAttribute("mensajeError", ex.getMessage());
            return "admin/seguridad/cambiar-password";
        }
    }

    private Usuario usuario(Authentication authentication) {
        return usuarioService.buscarPorUsername(authentication.getName());
    }
}
