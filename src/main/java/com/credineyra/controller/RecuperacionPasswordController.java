package com.credineyra.controller;

import com.credineyra.dto.RestablecerPasswordDto;
import com.credineyra.dto.SolicitudRecuperacionDto;
import com.credineyra.service.RecuperacionPasswordService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recuperar-password")
public class RecuperacionPasswordController {

    private final RecuperacionPasswordService service;

    public RecuperacionPasswordController(
            RecuperacionPasswordService service
    ) {
        this.service = service;
    }

    @GetMapping
    public String formularioSolicitud(
            Model model
    ) {

        model.addAttribute(
                "solicitudDto",
                new SolicitudRecuperacionDto()
        );

        return "auth/recuperar-password";
    }

    @PostMapping
    public String solicitar(
            @Valid
            @ModelAttribute("solicitudDto")
            SolicitudRecuperacionDto dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {
            return "auth/recuperar-password";
        }

        try {
            service.solicitarRecuperacion(dto.getEmail());
        } catch (RuntimeException ex) {
            model.addAttribute("mensajeError", "No se pudo enviar el código. Inténtalo nuevamente más tarde.");
            return "auth/recuperar-password";
        }

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Si el correo pertenece a una cuenta, enviamos un código temporal."
        );
        return "redirect:/recuperar-password/codigo";
    }

    @GetMapping("/codigo")
    public String formularioCodigo(Model model) {
        model.addAttribute("restablecerDto", new RestablecerPasswordDto());
        return "auth/restablecer-password";
    }

    @PostMapping("/codigo")
    public String restablecer(
            @Valid
            @ModelAttribute("restablecerDto")
            RestablecerPasswordDto dto,
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
            return "auth/restablecer-password";
        }

        try {
            service.restablecerPassword(
                    dto.getToken(), dto.getPassword(), dto.getConfirmarPassword()
            );
        } catch (RuntimeException ex) {
            model.addAttribute("mensajeError", ex.getMessage());
            return "auth/restablecer-password";
        }

        return "redirect:/login?passwordActualizada";
    }
}
