package com.credineyra.controller;

import com.credineyra.dto.PersonaDto;
import com.credineyra.entity.Persona;
import com.credineyra.service.PersonaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "personas",
                personaService.listarTodos()
        );

        return "admin/personas/lista";
    }

    @GetMapping("/nueva")
    public String nuevaPersona(Model model) {

        model.addAttribute(
                "personaDto",
                new PersonaDto()
        );

        return "admin/personas/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("personaDto")
            PersonaDto dto,
            BindingResult result
    ) {

        if (result.hasErrors()) {
            return "admin/personas/formulario";
        }

        Persona persona = new Persona();

        persona.setDni(dto.getDni());
        persona.setNombres(dto.getNombres());
        persona.setApellidos(dto.getApellidos());
        persona.setTelefono(dto.getTelefono());
        persona.setEmail(dto.getEmail());
        persona.setDireccion(dto.getDireccion());

        personaService.registrar(persona);

        return "redirect:/admin/personas";
    }

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute(
                "persona",
                personaService.buscarPorId(id)
        );

        return "admin/personas/detalle";
    }
}