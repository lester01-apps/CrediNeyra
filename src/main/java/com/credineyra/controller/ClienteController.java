package com.credineyra.controller;

import com.credineyra.dto.ClienteContactoDto;
import com.credineyra.dto.ClienteDetalleDto;
import com.credineyra.dto.ClienteEdicionDto;
import com.credineyra.dto.ClientePanelDto;
import com.credineyra.dto.ClienteRegistroDto;
import com.credineyra.entity.Cliente;
import com.credineyra.entity.enums.EstadoCliente;
import com.credineyra.exception.DatoDuplicadoException;
import com.credineyra.exception.OperacionNoPermitidaException;
import com.credineyra.exception.ReglaNegocioException;
import com.credineyra.service.ClienteContactoService;
import com.credineyra.service.ClienteDetalleService;
import com.credineyra.service.ClienteGestionService;
import com.credineyra.service.ClientePanelService;
import com.credineyra.service.ClienteRegistroService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/clientes")
public class ClienteController {

    private final ClientePanelService clientePanelService;

    private final ClienteRegistroService clienteRegistroService;

    private final ClienteDetalleService clienteDetalleService;

    private final ClienteGestionService clienteGestionService;

    private final ClienteContactoService clienteContactoService;


    public ClienteController(
            ClientePanelService clientePanelService,
            ClienteRegistroService clienteRegistroService,
            ClienteDetalleService clienteDetalleService,
            ClienteGestionService clienteGestionService,
            ClienteContactoService clienteContactoService
    ) {

        this.clientePanelService =
                clientePanelService;

        this.clienteRegistroService =
                clienteRegistroService;

        this.clienteDetalleService =
                clienteDetalleService;

        this.clienteGestionService =
                clienteGestionService;

        this.clienteContactoService =
                clienteContactoService;
    }


    /* =========================================================
       LISTADO
       ========================================================= */

    @GetMapping
    public String listar(
            Authentication authentication,
            Model model
    ) {

        ClientePanelDto panel =
                clientePanelService
                        .obtenerPanelClientes();


        model.addAttribute(
                "username",
                authentication.getName()
        );


        model.addAttribute(
                "seccionActiva",
                "clientes"
        );


        model.addAttribute(
                "panel",
                panel
        );


        model.addAttribute(
                "clientes",
                panel.getClientes()
        );


        model.addAttribute(
                "totalClientes",
                panel.getTotalClientes()
        );


        model.addAttribute(
                "clientesActivos",
                panel.getClientesActivos()
        );


        model.addAttribute(
                "clientesInactivos",
                panel.getClientesInactivos()
        );


        model.addAttribute(
                "clientesAdvertencia",
                panel.getClientesAdvertencia()
        );


        model.addAttribute(
                "clientesMora",
                panel.getClientesMora()
        );


        return "admin/clientes/lista";
    }


    /* =========================================================
       NUEVO CLIENTE
       ========================================================= */

    @GetMapping("/nuevo")
    public String nuevoCliente(
            Authentication authentication,
            Model model
    ) {

        if (!model.containsAttribute(
                "clienteRegistroDto"
        )) {

            model.addAttribute(
                    "clienteRegistroDto",
                    new ClienteRegistroDto()
            );
        }


        cargarDatosComunes(
                authentication,
                model
        );


        return "admin/clientes/formulario";
    }


    /* =========================================================
       GUARDAR NUEVO CLIENTE
       ========================================================= */

    @PostMapping("/guardar")
    public String guardar(
            @Valid
            @ModelAttribute("clienteRegistroDto")
            ClienteRegistroDto dto,

            BindingResult result,

            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {

            cargarDatosComunes(
                    authentication,
                    model
            );


            return "admin/clientes/formulario";
        }


        try {

            Cliente cliente =
                    clienteRegistroService
                            .registrarNuevoCliente(
                                    dto
                            );


            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Cliente "
                                    + cliente.getCodigoCliente()
                                    + " registrado correctamente."
                    );


            return "redirect:/admin/clientes";


        } catch (
                DatoDuplicadoException
                |
                ReglaNegocioException ex
        ) {

            model.addAttribute(
                    "errorNegocio",
                    ex.getMessage()
            );


            cargarDatosComunes(
                    authentication,
                    model
            );


            return "admin/clientes/formulario";
        }
    }


    /* =========================================================
       DETALLE
       ========================================================= */

    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {

        ClienteDetalleDto cliente =
                clienteDetalleService
                        .obtenerDetalle(
                                id
                        );


        cargarDatosComunes(
                authentication,
                model
        );


        model.addAttribute(
                "cliente",
                cliente
        );


        return "admin/clientes/detalle";
    }


    /* =========================================================
       ABRIR EDICION
       ========================================================= */

    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {

        ClienteEdicionDto dto =
                clienteGestionService
                        .obtenerParaEdicion(
                                id
                        );


        cargarDatosComunes(
                authentication,
                model
        );


        model.addAttribute(
                "clienteEdicionDto",
                dto
        );


        return "admin/clientes/editar";
    }


    /* =========================================================
       GUARDAR EDICION
       ========================================================= */

    @PostMapping("/{id}/editar")
    public String actualizar(
            @PathVariable Long id,

            @Valid
            @ModelAttribute("clienteEdicionDto")
            ClienteEdicionDto dto,

            BindingResult result,

            Authentication authentication,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {

            ClienteEdicionDto actual =
                    clienteGestionService
                            .obtenerParaEdicion(
                                    id
                            );


            restaurarCamposProtegidos(
                    dto,
                    actual
            );


            cargarDatosComunes(
                    authentication,
                    model
            );


            return "admin/clientes/editar";
        }


        try {

            Cliente actualizado =
                    clienteGestionService
                            .actualizarCliente(
                                    id,
                                    dto
                            );


            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Cliente "
                                    + actualizado.getCodigoCliente()
                                    + " actualizado correctamente."
                    );


            return "redirect:/admin/clientes/"
                    + id;


        } catch (
                DatoDuplicadoException
                |
                ReglaNegocioException ex
        ) {

            ClienteEdicionDto actual =
                    clienteGestionService
                            .obtenerParaEdicion(
                                    id
                            );


            restaurarCamposProtegidos(
                    dto,
                    actual
            );


            model.addAttribute(
                    "errorNegocio",
                    ex.getMessage()
            );


            cargarDatosComunes(
                    authentication,
                    model
            );


            return "admin/clientes/editar";
        }
    }


    /* =========================================================
       CAMBIAR ESTADO
       ========================================================= */

    @PostMapping("/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,

            @RequestParam
            EstadoCliente nuevoEstado,

            @RequestParam(
                    defaultValue = "lista"
            )
            String volver,

            RedirectAttributes redirectAttributes
    ) {

        try {

            Cliente cliente =
                    clienteGestionService
                            .cambiarEstado(
                                    id,
                                    nuevoEstado
                            );


            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "El cliente "
                                    + cliente.getCodigoCliente()
                                    + " ahora está "
                                    + cliente.getEstado()
                                    + "."
                    );


        } catch (
                OperacionNoPermitidaException
                |
                ReglaNegocioException ex
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            ex.getMessage()
                    );
        }


        return redireccionCliente(
                id,
                volver
        );
    }


    /* =========================================================
       ENVIAR CORREO
       ========================================================= */

    @PostMapping("/{id}/correo")
    public String enviarCorreo(
            @PathVariable Long id,

            @Valid
            @ModelAttribute
            ClienteContactoDto contacto,

            BindingResult result,

            @RequestParam(
                    defaultValue = "lista"
            )
            String volver,

            RedirectAttributes redirectAttributes
    ) {

        if (result.hasErrors()) {

            String mensajeError =
                    result.getAllErrors()
                            .isEmpty()
                            ? "Revise los datos del mensaje."
                            : result
                            .getAllErrors()
                            .get(0)
                            .getDefaultMessage();


            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            mensajeError
                    );


            return redireccionCliente(
                    id,
                    volver
            );
        }


        try {

            String correoDestino =
                    clienteContactoService
                            .enviarCorreo(
                                    id,
                                    contacto
                            );


            redirectAttributes
                    .addFlashAttribute(
                            "mensajeExito",
                            "Correo enviado correctamente a "
                                    + correoDestino
                                    + "."
                    );


        } catch (
                ReglaNegocioException ex
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "mensajeError",
                            ex.getMessage()
                    );
        }


        return redireccionCliente(
                id,
                volver
        );
    }


    /* =========================================================
       REDIRECCION
       ========================================================= */

    private String redireccionCliente(
            Long idCliente,
            String volver
    ) {

        if ("detalle".equalsIgnoreCase(
                volver
        )) {

            return "redirect:/admin/clientes/"
                    + idCliente;
        }


        return "redirect:/admin/clientes";
    }


    /* =========================================================
       RESTAURAR DATOS PROTEGIDOS
       ========================================================= */

    private void restaurarCamposProtegidos(
            ClienteEdicionDto destino,
            ClienteEdicionDto actual
    ) {

        destino.setIdCliente(
                actual.getIdCliente()
        );


        destino.setCodigoCliente(
                actual.getCodigoCliente()
        );


        destino.setDni(
                actual.getDni()
        );


        destino.setTipoOrigen(
                actual.getTipoOrigen()
        );


        destino.setEstado(
                actual.getEstado()
        );
    }


    /* =========================================================
       DATOS COMUNES
       ========================================================= */

    private void cargarDatosComunes(
            Authentication authentication,
            Model model
    ) {

        model.addAttribute(
                "username",
                authentication.getName()
        );


        model.addAttribute(
                "seccionActiva",
                "clientes"
        );
    }
}