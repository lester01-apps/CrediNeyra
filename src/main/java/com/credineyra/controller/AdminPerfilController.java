package com.credineyra.controller;

import com.credineyra.dto.AdminPerfilEdicionDto;
import com.credineyra.entity.Persona;
import com.credineyra.service.CreditoService;
import com.credineyra.service.AdministradorPerfilFotoService;
import com.credineyra.service.DocumentoStorageService;
import com.credineyra.service.PagoService;
import com.credineyra.service.PersonaService;
import com.credineyra.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@Controller
public class AdminPerfilController {

    private final UsuarioService usuarioService;
    private final CreditoService creditoService;
    private final PagoService pagoService;
    private final PersonaService personaService;
    private final AdministradorPerfilFotoService fotoService;
    private final DocumentoStorageService storageService;

    public AdminPerfilController(UsuarioService usuarioService, CreditoService creditoService,
                                 PagoService pagoService, PersonaService personaService,
                                 AdministradorPerfilFotoService fotoService,
                                 DocumentoStorageService storageService) {
        this.usuarioService = usuarioService;
        this.creditoService = creditoService;
        this.pagoService = pagoService;
        this.personaService = personaService;
        this.fotoService = fotoService;
        this.storageService = storageService;
    }

    @GetMapping("/admin/perfil")
    public String perfil(Authentication authentication, Model model) {
        var administrador = usuarioService.buscarPorUsername(authentication.getName());
        model.addAttribute("username", administrador.getUsername());
        model.addAttribute("administrador", administrador);
        model.addAttribute("rolesAdministrador", usuarioService.listarRolesUsuario(administrador.getIdUsuario()));
        model.addAttribute("usuariosSistema", usuarioService.listarTodos().size());
        model.addAttribute("creditosGestionados", creditoService.listarTodos().size());
        model.addAttribute("pagosRevisados", pagoService.contarPagos());
        model.addAttribute("administradorTieneFotoPerfil", fotoService.obtenerRuta(administrador).isPresent());
        model.addAttribute("seccionActiva", "perfil");
        return "admin/perfil";
    }

    @PostMapping("/admin/perfil/foto")
    public String cambiarFoto(
            @RequestParam("foto") MultipartFile foto,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            fotoService.guardarFotoPropia(usuarioService.buscarPorUsername(authentication.getName()), foto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu foto de perfil se actualizó correctamente.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No se pudo actualizar la foto. Inténtalo nuevamente en unos instantes."
            );
        }
        return "redirect:/admin/perfil";
    }

    @GetMapping("/admin/perfil/foto")
    public ResponseEntity<Void> verFoto(Authentication authentication) {
        var administrador = usuarioService.buscarPorUsername(authentication.getName());
        return fotoService.obtenerRuta(administrador)
                .map(ruta -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(storageService.crearUrlFirmada(ruta, 300)))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/perfil/editar")
    public String formularioEdicion(Authentication authentication, Model model) {
        var administrador = usuarioService.buscarPorUsername(authentication.getName());
        model.addAttribute("administrador", administrador);
        model.addAttribute("perfilDto", crearDto(administrador.getPersona()));
        return "admin/perfil/editar";
    }

    @PostMapping("/admin/perfil/editar")
    public String actualizarPerfil(@Valid @ModelAttribute("perfilDto") AdminPerfilEdicionDto perfilDto,
                                   BindingResult result,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        var administrador = usuarioService.buscarPorUsername(authentication.getName());
        if (result.hasErrors()) {
            model.addAttribute("administrador", administrador);
            return "admin/perfil/editar";
        }

        Persona actual = administrador.getPersona();
        Persona datosActualizados = new Persona();
        datosActualizados.setIdPersona(actual.getIdPersona());
        datosActualizados.setDni(actual.getDni());
        datosActualizados.setEmail(actual.getEmail());
        datosActualizados.setEstado(actual.getEstado());
        datosActualizados.setNombres(perfilDto.getNombres().trim());
        datosActualizados.setApellidos(perfilDto.getApellidos().trim());
        datosActualizados.setTelefono(normalizarOpcional(perfilDto.getTelefono()));
        datosActualizados.setDireccion(normalizarOpcional(perfilDto.getDireccion()));
        personaService.actualizar(datosActualizados);

        redirectAttributes.addFlashAttribute("mensajeExito", "Tus datos personales se actualizaron correctamente.");
        return "redirect:/admin/perfil";
    }

    private AdminPerfilEdicionDto crearDto(Persona persona) {
        AdminPerfilEdicionDto dto = new AdminPerfilEdicionDto();
        dto.setNombres(persona.getNombres());
        dto.setApellidos(persona.getApellidos());
        dto.setTelefono(persona.getTelefono());
        dto.setDireccion(persona.getDireccion());
        return dto;
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
