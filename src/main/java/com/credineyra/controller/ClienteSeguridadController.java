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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cliente")
public class ClienteSeguridadController {

    private final UsuarioService usuarioService;
    private final RecuperacionPasswordService recuperacionPasswordService;

    public ClienteSeguridadController(
            UsuarioService usuarioService,
            RecuperacionPasswordService recuperacionPasswordService
    ) {
        this.usuarioService = usuarioService;
        this.recuperacionPasswordService = recuperacionPasswordService;
    }

    @GetMapping
    public String inicio(Authentication authentication) {
        Usuario usuario = usuario(authentication);
        return Boolean.TRUE.equals(usuario.getCambioPasswordObligatorio())
                ? "cliente/seguridad/primer-acceso"
                : "redirect:/cliente/inicio";
    }

    @PostMapping("/seguridad/conservar-password")
    public String conservarPassword(Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        usuarioService.conservarPasswordInicial(usuario(authentication).getIdUsuario());
        redirectAttributes.addFlashAttribute("mensajeExito",
                "Puedes continuar usando tu contraseña actual. Podrás cambiarla más adelante desde tu perfil.");
        return "redirect:/cliente/pagos";
    }

    @PostMapping("/seguridad/solicitar-codigo")
    public String solicitarCodigo(Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            recuperacionPasswordService.solicitarCambioPassword(usuario(authentication));
            redirectAttributes.addFlashAttribute("mensajeExito",
                    "Te enviamos un código temporal a tu correo registrado.");
            return "redirect:/cliente/seguridad/cambiar-password";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No se pudo enviar el código. Verifica que tu correo registrado esté disponible."
            );
            return "redirect:/cliente/perfil";
        }
    }

    /** Garantiza el respaldo del formulario en GET, POST y al reenviar código. */
    @ModelAttribute("restablecerDto")
    public RestablecerPasswordDto prepararFormularioCambio() {
        return new RestablecerPasswordDto();
    }

    @GetMapping("/seguridad/cambiar-password")
    public String formularioCambio(Authentication authentication) {
        usuario(authentication);
        return "cliente/seguridad/cambiar-password";
    }

    @PostMapping("/seguridad/cambiar-password")
    public String cambiarPassword(@Valid @ModelAttribute("restablecerDto") RestablecerPasswordDto restablecerDto,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model,
                                  @RequestParam(required = false) String reenviar) {
        Usuario usuario = usuario(authentication);
        if (reenviar != null) {
            try {
                recuperacionPasswordService.solicitarCambioPasswordInicial(usuario);
                model.addAttribute("mensajeExito", "Enviamos un nuevo código a tu correo.");
            } catch (RuntimeException ex) {
                model.addAttribute("mensajeError", ex.getMessage());
            }
            return "cliente/seguridad/cambiar-password";
        }
        if (restablecerDto.getPassword() != null
                && !restablecerDto.getPassword().equals(restablecerDto.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "password.no.coincide", "Las contraseñas no coinciden");
        }
        if (result.hasErrors()) {
            return "cliente/seguridad/cambiar-password";
        }
        try {
            recuperacionPasswordService.restablecerPasswordPrimerIngreso(
                    usuario.getIdUsuario(), restablecerDto.getToken(),
                    restablecerDto.getPassword(), restablecerDto.getConfirmarPassword());
            return "redirect:/cliente/perfil?passwordActualizada";
        } catch (RuntimeException ex) {
            model.addAttribute("mensajeError", ex.getMessage());
            return "cliente/seguridad/cambiar-password";
        }
    }

    private Usuario usuario(Authentication authentication) {
        return usuarioService.buscarPorUsername(authentication.getName());
    }
}
