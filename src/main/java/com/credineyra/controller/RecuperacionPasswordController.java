package com.credineyra.controller;

import com.credineyra.dto.RestablecerPasswordDto;
import com.credineyra.dto.SolicitudRecuperacionDto;
import com.credineyra.service.RecuperacionPasswordService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
            Model model
    ) {

        if (result.hasErrors()) {
            return "auth/recuperar-password";
        }

        service.solicitarRecuperacion(
                dto.getEmail()
        );

        model.addAttribute(
                "mensaje",
                "Si el correo pertenece a una cuenta, recibirás instrucciones para recuperar tu contraseña."
        );

        return "auth/recuperar-password";
    }

    @GetMapping("/restablecer")
    public String formularioRestablecer(
            @RequestParam String token,
            Model model
    ) {

        service.validarToken(token);

        RestablecerPasswordDto dto =
                new RestablecerPasswordDto();

        dto.setToken(token);

        model.addAttribute(
                "restablecerDto",
                dto
        );

        return "auth/restablecer-password";
    }

    @PostMapping("/restablecer")
    public String restablecer(
            @Valid
            @ModelAttribute("restablecerDto")
            RestablecerPasswordDto dto,
            BindingResult result
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

        service.restablecerPassword(
                dto.getToken(),
                dto.getPassword(),
                dto.getConfirmarPassword()
        );

        return "redirect:/login?passwordActualizada";
    }
}