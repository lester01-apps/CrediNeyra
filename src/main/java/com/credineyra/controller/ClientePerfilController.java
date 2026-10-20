package com.credineyra.controller;

import com.credineyra.entity.Usuario;
import com.credineyra.service.ClientePagoPanelService;
import com.credineyra.service.ClientePerfilService;
import com.credineyra.service.DocumentoStorageService;
import com.credineyra.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Comparator;

@Controller
@RequestMapping("/cliente/perfil")
public class ClientePerfilController {

    private final UsuarioService usuarioService;
    private final ClientePagoPanelService panelService;
    private final ClientePerfilService perfilService;
    private final DocumentoStorageService storageService;

    public ClientePerfilController(
            UsuarioService usuarioService,
            ClientePagoPanelService panelService,
            ClientePerfilService perfilService,
            DocumentoStorageService storageService
    ) {
        this.usuarioService = usuarioService;
        this.panelService = panelService;
        this.perfilService = perfilService;
        this.storageService = storageService;
    }

    @GetMapping
    public String perfil(Authentication authentication, Model model) {
        Usuario usuario = usuario(authentication);
        var panel = panelService.obtener(usuario);
        var proximaCuota = panel.creditos().stream()
                .filter(c -> c.montoExigible() != null && c.montoExigible().signum() > 0)
                .min(Comparator.comparing(
                        c -> c.proximoVencimiento(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .orElse(null);
        BigDecimal totalPrestado = panel.creditos().stream()
                .map(c -> c.capitalOriginal() == null ? BigDecimal.ZERO : c.capitalOriginal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoPendiente = panel.creditos().stream()
                .map(c -> c.totalPendiente() == null ? BigDecimal.ZERO : c.totalPendiente())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long creditosActivos = panel.creditos().stream()
                .filter(c -> !"FINALIZADO".equals(c.estado()))
                .count();

        model.addAttribute("usuarioPerfil", usuario);
        model.addAttribute("panel", panel);
        model.addAttribute("creditoProximo", proximaCuota);
        model.addAttribute("totalPrestado", totalPrestado);
        model.addAttribute("saldoPendiente", saldoPendiente);
        model.addAttribute("creditosActivos", creditosActivos);
        model.addAttribute("pagosRecientes", panel.pagos().stream()
                .sorted(Comparator.comparing(
                        com.credineyra.dto.ClientePagoPanelDto.PagoFila::fechaPago,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .limit(3)
                .toList());
        model.addAttribute("tieneFotoPerfil", perfilService.obtenerFotoPropia(usuario).isPresent());
        model.addAttribute("clienteNombre", usuario.getPersona().getNombres());
        model.addAttribute("seccionActiva", "perfil");
        return "cliente/portal/perfil";
    }

    @PostMapping("/foto")
    public String cambiarFoto(
            @RequestParam("foto") MultipartFile foto,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            perfilService.guardarFotoPropia(usuario(authentication), foto);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tu foto de perfil se actualizó correctamente.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute(
                    "mensajeError",
                    "No se pudo actualizar la foto. Inténtalo nuevamente en unos instantes."
            );
        }
        return "redirect:/cliente/perfil";
    }

    @GetMapping("/foto")
    public ResponseEntity<Void> verFoto(Authentication authentication) {
        return perfilService.obtenerFotoPropia(usuario(authentication))
                .map(foto -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(storageService.crearUrlFirmada(foto.getRutaArchivo(), 300)))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private Usuario usuario(Authentication authentication) {
        return usuarioService.buscarPorUsername(authentication.getName());
    }
}
